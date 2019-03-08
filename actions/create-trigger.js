/**
  *
  * main() will be run when you invoke this action
  *
  * @param Cloud Functions actions accept a single parameter, which must be a JSON object.
  *
  * @return The output of this action, which must be a JSON object.
  *
  */

const openwhisk = require('openwhisk');
const request = require('request')
const feedName = '/whisk.system/alarms/alarm'

function main(params) {
    let ow = openwhisk()
    let triggerName = params.trigger
    let latitude = params.latitude
    let longitude = params.longitude
    let cron = params.cron
    let weather = params.weather
    let isAlertAll = params.isAlertAll
    let timezone = params.timezone

    let apikey = process.env.__OW_API_KEY
    let apihost = process.env.__OW_API_HOST

    let payloadParameters = {latitude, longitude, weather, isAlertAll, pushNotificationTag: triggerName}
    let triggerParameters = {cron, timezone, trigger_payload: payloadParameters}

    return new Promise((resolve, reject) => {
        createTriggerRequest(apikey, apihost, feedName, triggerName)
            .then(result => {
                return createFeed(ow, feedName, triggerName, triggerParameters)
            })
            .then(result => {
                return createRule(ow, "", "/_/push-notification-sample/get-weather-and-send-notification", triggerName)
            })
            .then(result => {
                resolve(result)
            })
            .catch(err => {
                reject(err)
            })
    })
}

function createTriggerRequest(apikey, apihost, feed, triggerName) {
  var requestOptions = {
    url: apihost + "/api/v1/namespaces/_/triggers/" + triggerName + "?overwrite=true",
    method: 'PUT',
    json: true,
    headers: {
        Authorization: 'Basic ' + Buffer.from(apikey).toString('base64')
    },
    body: {
      name: triggerName,
      annotations: [{key: "feed", value: feed}]
    }
  }

  return new Promise((resolve, reject) => {
    request(requestOptions, (error, response, body) => {
      if (error) {
        reject(error)
      }
      resolve(body)
    })
  })
}

function createFeed(ow, name, trigger, params) {
  return new Promise((resolve, reject) => {
    ow.feeds.create({name, trigger, params}).then(package => {
      resolve(package)
    }).catch(err => {
      reject(err)
    })
  })
}

function createRule(ow, name, action, trigger) {
    return new Promise((resolve, reject) => {
        ow.rules.create({name: trigger + "-get-weather-and-send-notification", action, trigger}).then(package => {
            resolve(package)
        }).catch(err => {
            reject(err)
        })
    })
}
