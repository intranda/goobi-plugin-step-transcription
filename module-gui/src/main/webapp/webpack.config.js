const path = require('path');
const webpack = require('webpack');

module.exports = {
    mode: 'production',
    entry: './resources/uii/dev/js/transcription.js',
    output: {
        path: path.resolve(__dirname, 'resources/uii/plugins/step/intranda_step_transcription/js'),
        filename: 'transcription.js'
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: ['babel-loader']
            },
            {
                test: /\.css$/i,
                use: ["style-loader", "css-loader"],
            },
        ]
    },
};