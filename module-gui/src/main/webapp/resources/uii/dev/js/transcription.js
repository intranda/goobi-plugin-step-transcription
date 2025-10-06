import { initQuill } from "./quill";

let useTilesBool = document.querySelector('[id$="useTiles"]').value === 'true' || false;

const showImage = () => {
    let imageUrl = document.querySelector('[id$="inputImageUrl"]').value;
    let configViewer = {
        element: "#bigImage",
        fittingMode: "fixed",
        useTiles: useTilesBool
    };
    let viewImage = new ImageView.Image( configViewer );
    let zoomControl = new ImageView.Controls.Zoom(viewImage);
    viewImage.load(imageUrl)
    .then((image) => {
        $('#ajaxloader_image').fadeOut(800);
    })
    .catch(() => {
        console.error( 'Error opening image', error );
        $('#ajaxloader_image').fadeOut(800);
        $('#' + configViewer.global.divId).html( 'Failed to load image: "' + error + '"' );
    });
};

const eventListenerActions = () => {
    showImage();
    initQuill();
};

document.addEventListener('DOMContentLoaded', function() {
    eventListenerActions();
});

// initialize image viewer and tinyMCE after ajax request
faces.ajax.addOnEvent(function (data) {
    if (data.status === 'success') {
        eventListenerActions();
    }
});
