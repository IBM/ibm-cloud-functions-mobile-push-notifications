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

  // filter for tomorrow's forecast only
  forecasts = forecasts.filter((element, index) => index > 7)

  if (forecasts.length > 0) {
      narrative = "Weather for tomorrow is: " + forecasts[0].weather[0].description
  }
  var text

  if (isAlertAll) {
      text = narrative
  } else if (weatherTypeSubscribedTo) {
      for (i in weatherTypeSubscribedTo) {
        for (j in forecasts) {
          if (forecasts[j].weather.description.toLowerCase().includes(weatherTypeSubscribedTo[i].toLowerCase())) {
              text = "Weather for tomorrow is: " + forecasts[j].weather[0].description
              console.log(narrative.toLowerCase())
              console.log(weatherTypeSubscribedTo[i].toLowerCase())
              break
          }
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
