import Quill from "quill";

import Toolbar from "quill/modules/toolbar";
import Snow from "quill/themes/snow";
import QuillTableBetter from "quill-table-better";

import "quill/dist/quill.snow.css";
import 'quill-table-better/dist/quill-table-better.css';

Quill.register({
    "modules/toolbar": Toolbar,
    'modules/table-better': QuillTableBetter,
    "themes/snow": Snow,
  }, true);

const toolbarOptions = [
    ['bold', 'italic', 'underline', 'strike'],        // toggled buttons
    ['blockquote', 'code-block'],
    [{ 'list': 'ordered'}, { 'list': 'bullet' }, { 'list': 'check' }],
    [{ 'script': 'sub'}, { 'script': 'super' }],      // superscript/subscript
    [{ 'indent': '-1'}, { 'indent': '+1' }],          // outdent/indent
    [{ 'size': ['small', false, 'large', 'huge'] }],  // custom dropdown
    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    [{ 'align': [] }],
    ['clean'],                                        // remove formatting button
    ['customTable'],
];


export const initQuill = () => {
    let quill = new Quill('#editor', {
        modules: {
            clipboard: {},
            history: {
                delay: 2000,
                userOnly: true,
            },
            toolbar: toolbarOptions,
            table: false,
            'table-better': {
                menus: ['column', 'row', 'merge', 'table', 'cell', 'wrap', 'copy', 'delete'],
            },
            keyboard: {
                bindings: {
                    tab: {
                        key: 9,
                        handler: (range, context) => {
                            if (context.format['code-block'] || context.format['blockquote']) {
                                return true;
                            }
                            const current = quill.getText(range.index, range.length);
                            const next = current + '    ';
                            quill.deleteText(range.index, range.length, 'silent');
                            quill.insertText(range.index, next, 'user');
                            quill.setSelection(range.index + next.length, 'silent');
                            return false;
                        }
                    }
                }
            }
        },
        theme: 'snow',
    });
    // get the content from the textarea, process it and set it to the quill editor
    const content = document.querySelector('.ocr-text').value;
    const delta = quill.clipboard.convert({ html: content });
    quill.updateContents(delta, 'silent');
    // let quill update the input submitting the current state of the text to the backend
    quill.on('text-change', function(delta, oldDelta, source) {
        const updatedContent = quill.getSemanticHTML();
        const textArea = document.querySelector('.ocr-text');
        const input = document.querySelector('.input_0');
        input.value = updatedContent;
        textArea.value = updatedContent;
    });
    // add custom binding to tab inserting 4 spaces
    // this is a workaround for the fact that quill does not support tabbing in the editor
    const tableModule = quill.getModule('table-better');
    const insertTable = (rows = 3, columns = 3) => {
        tableModule.insertTable(rows, columns);
    };
    const buildTableButton = () => {
        const btn = document.querySelector('.ql-customTable');
        // add icon to button
        const icon = document.createElement('span');
        icon.innerHTML = `<svg t="1692084293475" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2632" xmlns:xlink="http://www.w3.org/1999/xlink" width="16" height="16"><path d="M1012.62222223 944.76190506a78.01904747 78.01904747 0 0 1-78.01904747 78.01904747H76.3936505a78.01904747 78.01904747 0 0 1-78.01904747-78.01904747V86.55238079a78.01904747 78.01904747 0 0 1 78.01904747-78.01904746h858.20952426a78.01904747 78.01904747 0 0 1 78.01904747 78.01904746v858.20952427zM466.4888889 554.66666666H76.3936505v390.0952384h390.0952384V554.66666666z m468.11428586 0H544.50793636v390.0952384h390.0952384V554.66666666zM466.4888889 86.55238079H76.3936505v390.0952384h390.0952384V86.55238079z m468.11428586 0H544.50793636v390.0952384h390.0952384V86.55238079z" fill="#515151" p-id="2633"></path></svg>`;
        btn.appendChild(icon);
        btn.addEventListener('click', function() {
            insertTable();
        });
    };
    buildTableButton();
};
