/**
  *
  * main() will be run when you invoke this action
  *
  * @param Cloud Functions actions accept a single parameter, which must be a JSON object.
  *
  * @return The output of this action, which must be a JSON object.
  *
  */


const iam = require('@ibm-functions/iam-token-manager');
const request = require('request')
const tagsEndpoint = "/tags"

function main(params) {
    let pushParams = params.__bx_creds.imfpush
    let apikey = pushParams.apikey
    let pushUrl = pushParams.url
    let pushTag = params.tag
    let timezone = params.timezone
    let requestOptions = buildRequestOptions(pushUrl, tagsEndpoint, pushTag)

    // trigger params
    let latitude = params.latitude
    let longitude = params.longitude
    let trigger = pushTag
    let cron = params.cron
    let weather = params.weather
    let isAlertAll = params.isAlertAll

    return new Promise((resolve, reject) => {
        getAuthHeader(apikey)
            .then(header => {
                requestOptions.headers.Authorization = header
                request(requestOptions, (error, response, body) => {
                    if (error) {
                        reject(error)
                    }
                    body.latitude = latitude
                    body.longitude = longitude
                    body.trigger = pushTag
                    body.cron = cron
                    body.weather = weather
                    body.isAlertAll = isAlertAll
                    body.timezone = timezone
                    resolve(body)
                })
            }).catch(error => {
                reject(error)
            })
    })
}

function buildRequestOptions(url, endpoint, tag) {
    return {
        url: url + endpoint,
        method: 'POST',
        headers: {
            'Authorization': ''
        },
        json: true,
        body: {"description":"cron tag", "name": tag}
    }
}

function getAuthHeader(iamApiKey) {
const tm = new iam({
    iamApikey: iamApiKey,
    iamUrl: process.env.__OW_IAM_API_URL || 'https://iam.bluemix.net/identity/token',
});
return tm.getAuthHeader();
}
