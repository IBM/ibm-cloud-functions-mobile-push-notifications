/**
  *
  * main() will be run when you invoke this action
  *
  * @param Cloud Functions actions accept a single parameter, which must be a JSON object.
  *
  * @return The output of this action, which must be a JSON object.
  *
  */
function main(params) {
  let weatherTypeSubscribedTo = params.types
  let forecasts = params.weather.forecasts
  let pushNotificationTag = params.pushNotificationTag
  let isAlertAll = params.isAlertAll
  var narrative = ""

  forecasts = forecasts.filter(i => i.num == 2)

  if (forecasts.length > 0) {
      narrative = forecasts[0].narrative
  }
  var text

  if (isAlertAll) {
      text = narrative
  } else if (weatherTypeSubscribedTo) {
      for (i in weatherTypeSubscribedTo) {
          if (narrative.toLowerCase().includes(weatherTypeSubscribedTo[i].toLowerCase())) {
              text = narrative
              console.log(narrative.toLowerCase())
              console.log(weatherTypeSubscribedTo[i].toLowerCase())
              break
          }
      }
  }

  let messageText = text

  if (messageText) {
      return { status: "success", messageText, targetTagNames: [pushNotificationTag]}
  } else {
      return { error: "No match on forecasts. Nothing to send"}
  }
}
