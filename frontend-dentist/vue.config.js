const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // FRONTEND PORT
    port: 8070,
    proxy: {
      "/api": {
        target: 'http://localhost:8091',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  }
})
