export async function customFetch(
  reqMethod,
  pathUrl,
  headerParams = [],
  reqBody = ""
) {
  let init;

  switch (reqMethod) {
    case "GET":
      init = { method: "GET" };
      break;

    case "POST":
      if (headerParams.length !== 0) {
        let headerObj = new Headers();
        headerParams.forEach(header =>
          headerObj.set(Object.keys(header)[0], Object.values(header)[0])
        );

        init = {
          method: "POST",
          headers: headerObj,
          body: reqBody
        };
      } else {
        init = { method: "POST" };
      }
  }

  try {
    return await fetch(pathUrl, init);
  } catch (error) {
    // eslint-disable-next-line no-console
    return console.log(error);
  }
}

//Función que captura el valor de una variable del query string
export function getQueryVariable(variable) {
  var query = window.location.search.substring(1);
  var vars = query.split("&");
  for (var i = 0; i < vars.length; i++) {
    var pair = vars[i].split("=");
    if (pair[0] == variable) {
      return pair[1];
    }
  }
  return false;
}

export function emailIsValid(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}
