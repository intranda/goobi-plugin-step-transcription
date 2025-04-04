import { initQuill } from "./quill";

let useTilesBool = document.querySelector('[id$="useTiles"]').value === 'true' || false;

const showImage = () => {
    let imageUrl = document.querySelector('[id$="inputImageUrl"]').value;
    let configViewer = {
        global: {
            divId: 'bigImage',
            useTiles: useTilesBool,
            footerHeight: 0,
            adaptContainerHeight: false,
            zoomSlider: "#zoomSlider",
            zoomSliderHandle: "#zoomSlider .zoom-slider-handle",
            zoomSliderLabel: "#zoomSliderLabel input",
        },
        image: {
            mimeType: "image/jpeg",
            tileSource: imageUrl,
        },
    };
    let viewImage = new ImageView.Image( configViewer );
    viewImage.load()
    .then(function(image) {
        image.onFirstTileLoaded()
        .then(function() {
            $('#ajaxloader_image').fadeOut(800);
        })
        .catch(function() {
            $('#ajaxloader_image').fadeOut(800);
        })
    })
    .catch(function(error){
        console.error( 'Error opening image', error );
        $('#ajaxloader_image').fadeOut(800);
        $('#' + configViewer.global.divId).html( 'Failed to load image: "' + error + '"' );
    });
};

document.addEventListener('DOMContentLoaded', function() {
    showImage();
    initQuill();
});

// initialize image viewer and tinyMCE after ajax request
faces.ajax.addOnEvent(function (data) {
    if (data.status === 'success') {
        showImage();
        initQuill();
    }
});