package org.remdev.services.fileserver.controllers;

import org.remdev.services.fileserver.filters.ApiAuthFilter;
import org.remdev.services.fileserver.models.ResponseMessage;
import org.remdev.services.fileserver.models.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class FileAccessController {

    private static final Logger logger = LoggerFactory.getLogger(FileAccessController.class);

    @Value("${fileserver.storage.path}")
    private String uploadedFolder;

    @PostConstruct
    protected void init() {
        uploadedFolder = System.getProperty("user.home") + File.separator + uploadedFolder;
        File storageDir = new File(uploadedFolder);
        boolean dirsCreated = (storageDir.exists() && storageDir.isDirectory()) || storageDir.mkdirs();
        if (dirsCreated == false) {
            throw new RuntimeException("Could not prepare storage for files");
        }
    }

    //Single file upload
    @PostMapping("/api/upload")
    public ResponseEntity<ResponseMessage> uploadFile(
            @RequestAttribute(ApiAuthFilter.CLIENT_ID_ATTRIBUTE) String clientID,
            @RequestParam("file") MultipartFile uploadFile) {

        logger.debug("Single file upload!");

        if (uploadFile.isEmpty()) {
            return new ResponseEntity<>(ResponseMessage.error("Please select a file!"), HttpStatus.BAD_REQUEST);
        }
        try {
            saveUploadedFiles(clientID, Collections.singletonList(uploadFile));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/get-files")
    public ResponseEntity<List<FileInfo>> getFiles(
            @RequestAttribute(ApiAuthFilter.CLIENT_ID_ATTRIBUTE) String clientID) {

        File storageDir = new File(uploadedFolder + File.separator + clientID);
        if (storageDir.exists() == false || storageDir.isDirectory() == false) {
            return new ResponseEntity<>(Collections.<FileInfo>emptyList(), HttpStatus.OK);
        }
        List<FileInfo> fileInfos = Stream.of(storageDir.listFiles())
                .map(file -> new FileInfo(file.getName(), String.valueOf(file.length())))
                .collect(Collectors.toList());
        return new ResponseEntity<>(fileInfos, new HttpHeaders(), HttpStatus.OK);

    }

    @GetMapping("/api/get-file")
    public void getFiles(
            @RequestAttribute(ApiAuthFilter.CLIENT_ID_ATTRIBUTE) String clientID,
            @RequestParam("filename") String filename,
            HttpServletResponse response)
            throws IOException {

        File storageDir = new File(uploadedFolder + File.separator + clientID);
        if (storageDir.exists() == false || storageDir.isDirectory() == false) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Optional<File> responseFile = Stream.of(storageDir.listFiles())
                .filter(file -> file.getName().equals(filename))
                .findAny();
        if (responseFile.isPresent() == false) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        File file = responseFile.get();
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(file.length());
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byte[] buf = new byte[1 << 15];
            int read = -1;
            while ((read = inputStream.read(buf)) > -1) {
                response.getOutputStream().write(buf, 0, read);
            }
        } catch (Throwable e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void saveUploadedFiles(String clientID, List<MultipartFile> files) throws IOException {

        String dirPath = uploadedFolder + File.separator + clientID;
        File dir = new File(dirPath);
        if ((dir.exists() && dir.isFile()) || (dir.exists() == false && dir.mkdirs() == false)) {
            throw new RuntimeException("Storage is not ready");
        }
        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue; //next pls
            }

            byte[] bytes = file.getBytes();
            Path path = Paths.get(dirPath + File.separator + file.getOriginalFilename());
            Files.write(path, bytes);
        }
    }
}
