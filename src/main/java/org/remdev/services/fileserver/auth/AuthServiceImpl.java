package org.remdev.services.fileserver.auth;

import org.remdev.services.fileserver.models.ClientData;
import org.remdev.services.fileserver.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String LOGIN_FILE_DATA = "login.properties";
    private static final String TOKEN_FILE_DATA = "token.properties";

    private final Properties loginDataStorage = new Properties();
    private final Properties tokenDataStorage = new Properties();

    private String loginDataFile;
    private String tokenDataFile;

    public AuthServiceImpl(String dataDir) {
        Objects.requireNonNull(dataDir);

        String basePath = System.getProperty("user.home") + File.separator + dataDir;
        File dirFile = new File(basePath);
        boolean created = (dirFile.exists() && dirFile.isDirectory()) || dirFile.mkdirs();
        if (created == false) {
            throw new RuntimeException("Could not prepare storage for app data files");
        }
        loginDataFile = basePath + File.separator + LOGIN_FILE_DATA;
        tokenDataFile = basePath + File.separator + TOKEN_FILE_DATA;

        boolean loaded = loadData(loginDataStorage, loginDataFile) && loadData(tokenDataStorage, tokenDataFile);
        if (!loaded) {
            throw new RuntimeException("Could not prepare storage for app data files");
        }
        //FIXME: stub. remove me
        addClient("sergey", "admin");
        addClient("alex", "admin");
    }

    @Override
    public ClientData authenticate(String login, String password) {
        Optional<ClientData> data = Optional.ofNullable(loginDataStorage.getProperty(login))
                .map(storedData -> JsonUtils.fromJson(storedData, ClientData.class));

        if (data.map(d -> d.getPassword().equals(password)).orElse(false) == false) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        tokenDataStorage.setProperty(token, login);

        saveData(tokenDataStorage, tokenDataFile);

        ClientData clientData = data.get();
        clientData.setToken(token);
        return clientData;
    }

    @Override
    public synchronized ClientData removeToken(String token) {
        Object removedLogin = tokenDataStorage.remove(token);
        if (removedLogin == null) {
            return null;
        }
        String jsonData = loginDataStorage.getProperty(((String) removedLogin));
        ClientData data = JsonUtils.fromJson(jsonData, ClientData.class);
        loginDataStorage.remove(data.getLogin());
        saveData(loginDataStorage, loginDataFile);
        saveData(tokenDataStorage, tokenDataFile);
        data.setToken(token);
        return data;
    }

    @Override
    public ClientData getClient(String token) {
        return Optional.ofNullable(tokenDataStorage.getProperty(token))
                .map(loginDataStorage::getProperty)
                .map(json -> JsonUtils.fromJson(json, ClientData.class))
                .orElse(null);
    }

    public ClientData removeClient(String token) {
        return Optional.ofNullable(tokenDataStorage.getProperty(token))
                .map(loginDataStorage::getProperty)
                .map(json -> JsonUtils.fromJson(json, ClientData.class))
                .orElse(null);
    }

    @Override
    public synchronized ClientData addClient(String login, String password) {
        String token = UUID.randomUUID().toString();
        ClientData data = new ClientData();
        synchronized (this) {
            if (hasClient(login)) {
                return null;
            }
            data.setLogin(login);
            data.setPassword(password);
            data.setToken(token);
            loginDataStorage.setProperty(login, JsonUtils.toJson(data));
            saveData(loginDataStorage, loginDataFile);
        }
        tokenDataStorage.setProperty(token, login);
        saveData(tokenDataStorage, tokenDataFile);
        return data;
    }

    @Override
    public boolean hasClient(String login) {
        return loginDataStorage.containsKey(login);
    }

    private synchronized boolean loadData(Properties props, String file) {
        // skip for first time
        if (new File(file).exists() == false) {
            return true;
        }
        try (FileReader reader = new FileReader(file)) {
            props.load(reader);
            return true;
        } catch (IOException e) {
            log.warn("Could not load data from {}, exception = {}", file, e.getMessage());
            return false;
        }
    }

    private synchronized void saveData(Properties props, String file) {
        try (FileWriter writer = new FileWriter(file)) {
            props.store(writer, null);
        } catch (IOException e) {
            log.warn("Could not save data to {}, exception = {}", file, e.getMessage());
        }
    }
}
