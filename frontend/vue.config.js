const { defineConfig } = require('@vue/cli-service')


module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    //FRONTEND PORT
    port: 8080,
    proxy: {
      "/api": {
        target: process.env.VUE_APP_APIGATEWAY_URL,
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
        }}}
      });

