function searchTours() {
  var query = $('#search-query').val();
  if (query == undefined || query == null || query == "") {
    window.location="/tours/all";
    return;
  }
  window.location="/tours/search?query=" + encodeURI(query);
}

function openTour(id) {
  window.location="/tours/detail?id=" + id;
}
