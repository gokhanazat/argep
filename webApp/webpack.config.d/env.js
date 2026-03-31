const webpack = require('webpack');
const dotenv = require('dotenv');
const path = require('path');

// Root'taki .env dosyasını oku
const env = dotenv.config({ path: path.resolve(__dirname, '../../../../.env') }).parsed || {};

// DefinePlugin ile injection
config.plugins.push(
    new webpack.DefinePlugin({
        'process.env.SUPABASE_URL': JSON.stringify(env.SUPABASE_URL || ''),
        'process.env.SUPABASE_ANON_KEY': JSON.stringify(env.SUPABASE_ANON_KEY || ''),
    })
);
