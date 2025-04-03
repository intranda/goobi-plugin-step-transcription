import Quill from "quill";

import Toolbar from "quill/modules/toolbar";
import Snow from "quill/themes/snow";

Quill.register({
    "modules/toolbar": Toolbar,
    "themes/snow": Snow,
  });

export const initQuill = () => {
    let quill = new Quill('#editor', {
        modules: {
            history: {
                delay: 2000,
                userOnly: true,
            },
            toolbar: true,
        },
        theme: 'snow',
    });
    const content = document.querySelector('.ocr-text').value;
    quill.clipboard.dangerouslyPasteHTML(content);
    quill.on('text-change', function(delta, oldDelta, source) {
        const updatedContent = quill.getSemanticHTML();
        const inputEl = document.querySelector('.input_0');
        inputEl.value = updatedContent;
        const textArea = document.querySelector('.ocr-text');
        textArea.value = updatedContent;
    });
};