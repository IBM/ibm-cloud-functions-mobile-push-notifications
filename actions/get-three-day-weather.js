// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements; and to You under the Apache License, Version 2.0.

var request = require('request');

/**
 * Get hourly weather forecast for a lat/long from the Weather API service.
 *
 * Must specify one of zipCode or latitude/longitude.
 *
 * @param username The Weather service API account username.
 * @param username The Weather service API account password.
 * @param latitude Latitude of coordinate to get forecast.
 * @param longitude Longitude of coordinate to get forecast.
 * @param zipCode ZIP code of desired forecast.
 * @return The hourly forecast for the lat/long.
 */
function main(params) {
    console.log('input params:', params);
    var openweatherapikey = params.openweatherapikey;
    var lat = params.latitude || '0';
    var lon = params.longitude ||  '0';
    var url = 'https://api.openweathermap.org/data/2.5/forecast?lat=' + lat + '&lon=' + lon + '&cnt=16&appid=' + openweatherapikey

    console.log('url:', url);

    var promise = new Promise(function(resolve, reject) {
        request({
            url: url,
            timeout: 30000
        }, function (error, response, body) {
            if (!error && response.statusCode === 200) {
                var j = JSON.parse(body);
                var result = {}
                result.weather = {}
                result.weather.forecasts = j.list
                result.isAlertAll = params.isAlertAll
                result.types = params.weather
                result.pushNotificationTag = params.pushNotificationTag
                resolve(result);
            } else {
                console.log('error getting forecast');
                console.log('http status code:', (response || {}).statusCode);
                console.log('error:', error);
                console.log('body:', body);
                reject({
                    error: error,
                    response: response,
                    body: body
                });
            }
        });
    });

    return promise;
}
