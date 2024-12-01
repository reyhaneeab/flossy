const { defineConfig } = require('@vue/cli-service')


module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    //FRONTEND PORT
    port: 8080,
    proxy: {
      "/user-service": {
        target: process.env.VUE_APP_USER_SERVICE_URL,
        changeOrigin: true,
        pathRewrite: {
          '^/user-service': ''
        } },
        "/appointment-service": {
          target: process.env.VUE_APP_APPOINTMENT_SERVICE_URL,
          changeOrigin: true,
          pathRewrite: {
            '^/appointment-service': ''
          }},
          "/calendar-service": {
            target: process.env.VUE_APP_CALENDAR_SERVICE_URL,
            changeOrigin: true,
            pathRewrite: {
              '^/calendar-service': ''
            }},
            "/notification-service": {
              target: process.env.VUE_APP_NOTIFICATION_SERVICE_URL,
              changeOrigin: true,
              pathRewrite: {
                '^/notification-service': ''
              }
            },
          },
        },
      });

