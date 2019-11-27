//log out fetch
function logOut(evt) {
  evt.preventDefault();

  customFetch("POST", "/api/logout")
    .then(response => {
      if (response.ok) {
        location.href = "/web/boarding_page.html";
      }
    })
    .catch(error => console.log(error));
}

//Log out
let logOutBtn = document.getElementById("logOutBtn");
logOutBtn.addEventListener("click", logOut);

//Back to games
let backToGamesBtn = document.getElementById("backToGames");
backToGamesBtn.addEventListener("click", () => {
  location.href = "/web/games.html";
});

//-----------------------------------------------------------------------------------

//create game fetch
function createGame(evt) {
  evt.preventDefault();

  customFetch("POST", "/api/games")
    .then(response => {
      if (response.status == 201) {
        response.json().then(data => goToGame(data.gpId));
      } else {
        alert("Something went south, try again later");
      }
    })
    .catch(error => console.log(error));
}

//Botón de creación de juegos
let gameCreator = document.getElementById("gameCreator");
gameCreator.addEventListener("click", createGame);

//-----------------------------------------------------------------------------------

//Función para boton Join Game
//join game fetch
function joinGame(gameId) {
  customFetch("POST", "/api/game/" + gameId + "/players")
    .then(response => {
      if (response.ok) {
        response.json().then(body => goToGame(body.gpId));
      } else {
        alert("Yeah... couldn't. Try again some other time");
      }
    })
    .catch(error => console.log(error));
}

//go to game
function goToGame(gpId) {
  window.location.search = "gp=" + gpId;
}

//-----------------------------------------------------------------------------------

//contenedor para barcos ubicados
let shipsForPost = [];

//Envío de lista de barcos
async function postShipList(evt) {
  evt.preventDefault();

  if (shipsForPost.length == 5) {
    let gpId = getQueryVariable("gp");

    customFetch(
      "POST",
      "/api/games/players/" + gpId + "/ships",
      [
        {
          "Content-Type": "application/json;charset=UTF-8"
        }
      ],
      JSON.stringify(shipsForPost)
    )
      .then(response => {
        if (response.ok) {
          window.location.reload();
        }
      })
      .catch(error => console.log(error));
  } else {
    alert("Place all your ships to continue");
  }
}

// Boton de envío de lista de barcos
let placeShips = document.getElementById("placeShips");
placeShips.addEventListener("click", postShipList);

//-----------------------------------------------------------------------------------
//Envio de salvos
//Array contenedor de salvoes
let salvoesForPost = [];
//Envío de lista de barcos
async function postSalvoesList(evt) {
  evt.preventDefault();

  if (salvoesForPost.length != 0 && salvoesForPost.length <= 5) {
    let gpId = getQueryVariable("gp");

    customFetch(
      "POST",
      "/api/games/players/" + gpId + "/salvoes",
      [
        {
          "Content-Type": "application/json;charset=UTF-8"
        }
      ],
      JSON.stringify(salvoesForPost)
    )
      .then(response => {
        if (response.ok) {
          window.location.reload();
        }
      })
      .catch(error => console.log(error));
  } else {
    alert("Could not send salvoes");
  }
}

// Boton de envío de lista de barcos
let fireSalvoesBtn = document.getElementById("fireSalvoes");
fireSalvoesBtn.addEventListener("click", postSalvoesList);

//-----------------------------------------------------------------------------------
//Fetch de Games Info, creación de la tabla de juegos y agrega el nombre de usuario al header
//Variable de datos
let gamesCallData;

function gamesInfo() {
  fetch("/api/games")
    .then(response => response.json())
    .then(data => {
      gamesCallData = data;
      currentGamesList(gamesCallData);
      document.getElementById("playerSpan").innerText =
        gamesCallData.currentUser.email;
    })
    .catch(error => console.log(error));
}

//-----------------------------------------------------------------------------------

//Si hay query string, lo captura y realiza el pedido de datos
//Almacenaje de datos para prueba
let gpById;

function fetchByQueryString() {
  let queryStringGP = getQueryVariable("gp");

  if (queryStringGP) {
    document.getElementsByClassName("data")[0].classList.toggle("hide");
    document.getElementsByClassName("grids")[0].classList.toggle("hide");
    document.getElementsByClassName("fleetStatus")[0].classList.toggle("hide");

    createGrid("player-grid", "p");
    createGrid("salvos-grid", "s");

    salvoesEventListener();

    customFetch("GET", "/api/game_view/" + queryStringGP)
      .then(response => {
        if (response.ok) {
          response.json().then(data => {
            gpById = data;
            playerNames(data, queryStringGP);

            if (data.ships.length != 0) {
              data.ships.forEach(ship => paintThemShips(ship));

              document.querySelector(".available-ships").classList.add("hide");

              document.getElementById("placeShips").classList.add("hide");
            } else {
              document.getElementById("salvoes").classList.add("hide");
            }

            if (Object.keys(data.salvoes).length != 0) {
              paintThemSalvoes(data, queryStringGP);
              paintThemHits(data, queryStringGP);
              fleetReport(data);
            }

            checkForChanges(
              data.game_state,
              data.game_turn,
              data.game_player_state
            );
          });
        } else {
          window.location.href = "/web/boarding_page.html";
        }
      })
      .catch(error => console.log(error));
  }
}

//-----------------------------------------------------------------------------------
//Chequeo de cambios en el juego
function checkForChanges(previousState, previousTurn, previousGPState) {
  var intervalId = setInterval(changesChange, 5000);

  function changesChange() {
    customFetch("GET", "/api/game_state/" + getQueryVariable("gp")).then(
      response => {
        if (response.ok) {
          response.json().then(data => {
            if (
              previousState != data.game_state ||
              previousTurn == previousTurn + 1 ||
              previousGPState != data.game_player_state
            ) {
              location.reload(true);
            } else if (data.game_state == "over") {
              window.clearInterval(intervalId);
            }
          });
        }
      }
    );
  }
}

//-----------------------------------------------------------------------------------

//Creación de tabla de juegos en curso
//OK, el asunto es así:
// De la información de /api/games va a hacer una tabla, cada fila va a ser uno de los juegos.
// Va a extraer los nombres de los jugadores de cada game player.
// Después, si hay solo 1 game player, crea una celda vacía para mantener el orden de la tabla.
// Luego de crear la celda de la fecha de creación, crea una celda más para el botón de unirse;
// Si el partido esta completo (esta lógica es al momento del task 2), y si el usuario no participa,
// anota que la partida esta llena. Si el usuario participa del juego, crea un boton que lo envía a ese game_view
// extrayendo el GPID de la iteración del bucle.
// Si hay un solo game player, el boton dirá de unirse y su acción sera de crear un juego en el servidor e ir a el
// game_view respectivo

function currentGamesList(data) {
  let tableBody = document.getElementById("gamesList");

  data.games.forEach(game => {
    let gameRow = document.createElement("tr");

    let gameId = document.createElement("td");
    gameId.innerText = game.id;
    gameRow.appendChild(gameId);

    game.game_players.forEach(gp => {
      let player = document.createElement("td");
      player.innerText = gp.player.email;
      player.setAttribute("data-id", gp.player.id);
      gameRow.appendChild(player);
    });

    if (game.game_players.length == 1) {
      gameRow.appendChild(document.createElement("td"));
    }

    let gameCreation = document.createElement("td");
    gameCreation.innerText = game.created;
    gameRow.appendChild(gameCreation);

    let joinCell = document.createElement("td");
    gameRow.appendChild(joinCell);

    let playerInGame = game.game_players
      .map(gp => gp.player.id)
      .includes(data.currentUser.id);

    if (game.game_state == "over") {
      joinCell.innerHTML = `<p>Game Over</p>`;
    } else if (
      (game.game_state == "ship" || game.game_state == "salvo") &&
      !playerInGame
    ) {
      joinCell.innerHTML = `<p>Game Full</p>`;
    } else if (game.game_state != "over" && playerInGame) {
      let joinGameBtn = document.createElement("button");
      joinGameBtn.innerText = "Re-Join Game";
      joinGameBtn.classList.add("join", "list-button");

      let gpId = game.game_players.filter(
        gp => gp.player.id == data.currentUser.id
      )[0].id;

      joinGameBtn.setAttribute("onclick", `goToGame(${gpId})`);

      joinCell.appendChild(joinGameBtn);
    } else if (game.game_state == "waiting_p2") {
      let joinGameBtn = document.createElement("button");
      joinGameBtn.innerText = "Join Game";
      joinGameBtn.classList.add("join", "list-button");
      joinGameBtn.setAttribute("onclick", `joinGame(${game.id})`);

      joinCell.appendChild(joinGameBtn);
    }

    tableBody.appendChild(gameRow);
  });
}

//-----------------------------------------------------------------------------------

//Función de creación de la grilla, recibe Id de contenedor y target para player o salvo
//Por cada letra crea una linea y le pone id's y clases
function createGrid(id, target) {
  //Contenedor de la grilla
  let grid = document.getElementById(id);
  //Patrón de letras
  let gridLetters = ["0", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j"];

  gridLetters.forEach((letter, i) => {
    let squareLine = document.createElement("div");
    squareLine.setAttribute("class", "grid-line " + letter);

    for (i = 0; i < 11; i++) {
      let square = document.createElement("div");
      square.setAttribute("id", target + letter + i);
      square.setAttribute("class", "grid-square");

      if (
        square.getAttribute("id") == "s00" ||
        square.getAttribute("id") == "p00"
      ) {
        square.classList.add("blank");
      } else if (letter === "0") {
        square.classList.add("column-name");
        square.innerHTML = `<p>${i}</p>`;
      } else if (i === 0) {
        square.classList.add("column-num");
        square.innerHTML = `<p>${letter.toUpperCase()}</p>`;
      } else if (target == "p") {
        square.classList.add("wah");
      } else if (target == "s") {
        square.classList.add("battle-square");
      }

      squareLine.appendChild(square);
    }

    grid.appendChild(squareLine);
  });
}

//Función que cambia el color si hay una pieza de barco en player's grid
//Se usa toLowerCase para evitar posibles errores de mayusculas
function paintThemShips(ship) {
  ship.locations.forEach(location => {
    document
      .getElementById("p" + location.toLowerCase())
      .classList.add("piece");
  });
}

//Función que marca salvoes en salvo's grid
//Se usa toLowerCase para evitar posibles errores de mayusculas
function paintThemSalvoes(data, gamePlayerId) {
  let p1Id = data.game_players.filter(
    gp => gp.game_player_id == gamePlayerId
  )[0].player_detail.id;

  Object.entries(data.salvoes[p1Id]).forEach(turn => {
    turn[1].forEach(location => {
      salvoLocation = document.getElementById("s" + location.toLowerCase());
      salvoLocation.classList.add("salvo");
      salvoLocation.innerHTML = `<p>${turn[0]}</p>`;
    });
  });
}

//Función de hits
//Se usa toLowerCase para evitar posibles errores de mayusculas
function paintThemHits(data, gamePlayerId) {
  let p2Id = data.game_players.filter(
    gp => gp.game_player_id != gamePlayerId
  )[0].player_detail.id;

  if (data.salvoes[p2Id]) {
    Object.entries(data.salvoes[p2Id]).forEach(turn => {
      turn[1].forEach(location => {
        hitLocation = document.getElementById("p" + location.toLowerCase());

        if (hitLocation.classList.contains("piece")) {
          hitLocation.classList.add("hit");
          hitLocation.innerHTML = `<p>${turn[0]}</p>`;
        }
      });
    });
  }
}

//Función que coloca nombre de los jugadores
function playerNames(data, gpId) {
  let p1Name = Array.from(document.getElementsByClassName("player1"));
  let p2Name = Array.from(document.getElementsByClassName("player2"));

  data.game_players.forEach(gp => {
    if (gp.game_player_id == gpId) {
      p1Name.forEach(e => (e.innerText = gp.player_detail.email));
    } else {
      p2Name.forEach(e => (e.innerText = gp.player_detail.email));
    }
  });
}

//-----------------------------------------------------------------------------------
//Live feed de progreso de partida
//En base a fleetstatus del game view

function fleetReport(data) {
  let queryStringGP = getQueryVariable("gp");

  let p1Id = data.game_players.filter(
    gp => gp.game_player_id == queryStringGP
  )[0].player_detail.id;

  let p2Id = data.game_players.filter(
    gp => gp.game_player_id != queryStringGP
  )[0].player_detail.id;

  liveFeed("player1fleet", data.fleet_status[p1Id]);

  liveFeed("player2fleet", data.fleet_status[p2Id]);
}

function liveFeed(containerId, arrayInfo) {
  arrayInfo.forEach(turn => {
    if (turn.hits.lenght != 0) {
      turn.hits.forEach(hit => {
        let turnRow = document.createElement("tr");

        let turnCell = document.createElement("td");
        turnCell.innerHTML = `<p>${turn.turn}</p>`;

        let shipTypeCell = document.createElement("td");
        shipTypeCell.innerHTML = `<p>${hit.ship}</p>`;

        let dmgCell = document.createElement("td");

        if (hit.sunk) {
          dmgCell.innerHTML = `<p>Sunk!</p>`;
          document.getElementById(
            containerId + "Brief"
          ).innerHTML += `<li><p>${hit.ship}</p></li>`;
        } else {
          dmgCell.innerHTML = `<p>${hit.dmg.length}</p>`;
        }

        turnRow.appendChild(turnCell);
        turnRow.appendChild(shipTypeCell);
        turnRow.appendChild(dmgCell);

        document.getElementById(containerId).appendChild(turnRow);
      });
    }
  });
}

//---------------------------------------------------------------------------------
// Ship placement madness
// Drag'n Droppin

//Variable necesaria para transportar el ID a los eventos donde no llega DataTransfer
let draggedItemId;

//Necesario para pasar info de posiciones de barco durante el drag
let shipLocations;

//Ubicacion temporal del barco movido en caso q el destino no sea valido
let provisoryShip;

/* Eventos sobre elemento arrastrado */
document.addEventListener(
  "drag",
  function(event) {
    let ship = event.target;
    ship.classList.add("hide");
  },
  false
);

document.addEventListener(
  "dragstart",
  function(event) {
    //referencia de elemento arrastrado, no todos los eventListener tienen acceso
    event.dataTransfer.setData("text/plain", event.target.id);
    event.dataTransfer.effectAllowed = "move";

    draggedItemId = event.dataTransfer.getData("text");

    let cell = event.target.parentElement;

    if (cell.classList.contains("piece")) {
      let availableSpace = availableSpaceTakingIntoAccountShipRotation(
        cell,
        draggedItemId
      );

      let data = fits(draggedItemId, availableSpace);

      provisoryShip = data.positions;

      data.positions.forEach(square => square.classList.remove("piece"));

      event.target.nextSibling.remove();
    }
  },
  false
);

document.addEventListener(
  "dragend",
  function(event) {
    let ship = event.target;

    ship.classList.remove("hide");
  },
  false
);

/*Eventos sobre el contenedor destino*/
//efecto permitido del contenedor destino drop/no drop
document.addEventListener(
  "dragover",
  function(event) {
    let cell = event.target;
    if (cell.classList.contains("wah")) {
      // prevent default to allow drop
      event.preventDefault();
      event.dataTransfer.effectAllowed = "move";
    } else {
      event.preventDefault();
      event.dataTransfer.effectAllowed = "none";
    }
  },
  false
);

//Lógica de mostrar posiciones permitidas
document.addEventListener(
  "dragenter",
  function(event) {
    let cell = event.target;

    if (cell.classList.contains("wah")) {
      event.preventDefault();

      let availableSpace = availableSpaceTakingIntoAccountShipRotation(
        cell,
        draggedItemId
      );
      let data = fits(draggedItemId, availableSpace);

      if (data.fits) {
        shipLocations = data.positions;

        shipLocations.forEach(square => {
          if (!square.classList.contains("piece")) {
            square.classList.add("space");
            square.classList.remove("noSpace");
          } else {
            square.classList.add("noSpace");
          }
        });
      } else {
        shipLocations = availableSpace;
        availableSpace.forEach(square => square.classList.add("noSpace"));
      }
    } else {
      Array.from(document.getElementsByClassName("wah")).forEach(square =>
        square.classList.remove("space", "noSpace")
      );
    }
  },
  false
);

document.addEventListener(
  "dragleave",
  function(event) {
    //Resetear celdas cuando se quita el barco del lugar
    let cell = event.target;

    if (cell.classList.contains("wah")) {
      let availableSpace = availableSpaceTakingIntoAccountShipRotation(
        cell,
        draggedItemId
      );

      let removeData = fits(draggedItemId, availableSpace);

      removeData.positions
        .filter(square => !shipLocations.includes(square))
        .forEach(square => square.classList.remove("space", "noSpace"));
    }
  },
  false
);

document.addEventListener(
  "drop",
  function(event) {
    let cell = event.target;
    let ship = document.getElementById(event.dataTransfer.getData("text"));

    //Si es en un lugar permitido
    if (cell.classList.contains("wah")) {
      // prevent default
      event.preventDefault();

      if (dropPossible(shipLocations)) {
        cell.appendChild(ship);

        shipLocations.forEach(square => {
          square.classList.remove("space");
          square.classList.add("piece");
        });

        addShipToArray(shipsForPost, ship.id, shipLocations);

        addRotateBtn(cell, draggedItemId);

        console.log("drop succesfull");
      } else {
        if (provisoryShip.length != 0) {
          provisoryShip.forEach(square => {
            square.classList.add("piece");
          });

          addRotateBtn(provisoryShip[0], draggedItemId);
        }

        console.log("drop failed, orangy thingy");
      }

      Array.from(document.getElementsByClassName("wah")).forEach(square =>
        square.classList.remove("space", "noSpace")
      );
      event.dataTransfer.clearData();
      draggedItemId = "";
      shipLocations = [];
      provisoryShip = [];
    } else {
      if (provisoryShip.length != 0) {
        provisoryShip.forEach(square => {
          square.classList.add("piece");
        });

        addRotateBtn(provisoryShip[0], draggedItemId);
      }
      console.log("drop failed, not valid area");

      Array.from(document.getElementsByClassName("wah")).forEach(square =>
        square.classList.remove("space", "noSpace")
      );
      event.dataTransfer.clearData();
      draggedItemId = "";
      shipLocations = [];
      provisoryShip = [];
    }
  },
  false
);

//Si hay espacio suficiente, devuelve true y un array de posiciones del barco
function fits(id, array) {
  switch (id) {
    case "carrier":
      return array.length >= 5
        ? {
            fits: true,
            positions: array.slice(0, 5)
          }
        : {
            fits: false,
            positions: array
          };
    case "battleship":
      return array.length >= 4
        ? {
            fits: true,
            positions: array.slice(0, 4)
          }
        : {
            fits: false,
            positions: array
          };
    case "destroyer":
      return array.length >= 3
        ? {
            fits: true,
            positions: array.slice(0, 3)
          }
        : {
            fits: false,
            positions: array
          };
    case "submarine":
      return array.length >= 3
        ? {
            fits: true,
            positions: array.slice(0, 3)
          }
        : {
            fits: false,
            positions: array
          };
    case "patrolboat":
      return array.length >= 2
        ? {
            fits: true,
            positions: array.slice(0, 2)
          }
        : {
            fits: false,
            positions: array
          };
  }
}
//true si es una posición válida

function dropPossible(array) {
  return (
    array.every(square => !square.classList.contains("piece")) &&
    array.every(square => !square.classList.contains("noSpace"))
  );
}

//botton para rotar barcos
function addRotateBtn(cell, imgId) {
  let btn = document.createElement("button");
  btn.setAttribute("class", "rotate-btn");
  btn.setAttribute("onclick", `rotateShip("${imgId}")`);

  cell.appendChild(btn);
}

//función que agrega la clase para rotar barcos
function rotateShip(imgId) {
  let shipImg = document.getElementById(imgId);
  let cell = shipImg.parentElement;
  let availableSpace;
  let data;

  if (cell.classList.contains("piece")) {
    availableSpace = availableSpaceTakingIntoAccountShipRotation(cell, imgId);

    data = fits(imgId, availableSpace);

    provisoryShip = data.positions;

    data.positions.forEach(square => square.classList.remove("piece"));
  }

  shipImg.classList.toggle("rotated");

  posibleSpace = availableSpaceTakingIntoAccountShipRotation(cell, imgId);

  posibleData = fits(imgId, posibleSpace);

  if (dropPossible(posibleData.positions) && posibleData.fits) {
    posibleData.positions.forEach(square => square.classList.add("piece"));

    addShipToArray(shipsForPost, imgId, posibleData.positions);
  } else {
    shipImg.classList.toggle("rotated");

    provisoryShip.forEach(square => square.classList.add("piece"));
  }
}

//funcion para transformar posiciones
function availableSpaceTakingIntoAccountShipRotation(cell, shipId) {
  let ship = document.getElementById(shipId);

  if (ship.classList.contains("rotated")) {
    let gridLetters = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j"];

    let gridLetterNumber = gridLetters.indexOf(cell.id.slice(1, 2));

    let grid = Array.from(document.getElementsByClassName("wah"));

    let gridRow = grid.filter(square => square.id.slice(2) == cell.id.slice(2));

    return gridRow.slice(gridLetterNumber);
  } else {
    let gridRow = cell.parentElement.children;

    return Array.from(gridRow).slice(cell.id.slice(2));
  }
}

//Agregar barco a array para envio
function addShipToArray(shipArr, shipId, locations) {
  let ids = shipArr.map(ship => ship.shipType);

  if (shipArr.length == 0 || !ids.includes(shipId)) {
    shipArr.push({
      shipType: shipId,
      shipLocations: locations.map(loc => loc.id.slice(1))
    });
  } else {
    let index = shipArr.findIndex(el => el.shipType == shipId);
    shipArr[index].shipLocations = locations.map(loc => loc.id.slice(1));
  }
}

//-----------------------------------------------------------------------------------
//Ubicación de salvoes
function addSalvoToList(evt) {
  if (
    !evt.target.classList.contains("salvo") &&
    !evt.target.classList.contains("shot") &&
    salvoesForPost.length < 5
  ) {
    evt.target.classList.add("shot");

    salvoesForPost.push(evt.target.id.slice(1));
  } else if (evt.target.classList.contains("shot")) {
    evt.target.classList.remove("shot");

    salvoesForPost.splice(salvoesForPost.indexOf(evt.target.id.slice(1)), 1);
  } else {
    alert("Only five shots per round");
  }
}

//Agrega el event listener on click a la grilla
function salvoesEventListener() {
  let battleGrid = Array.from(document.getElementsByClassName("battle-square"));

  battleGrid.forEach(square =>
    square.addEventListener("click", addSalvoToList)
  );
}

//-----------------------------------------------------------------------------------

//Utilities
// Funcion generica de fetch con constuccion de header
async function customFetch(
  reqMethod,
  pathUrl,
  headerParams = [],
  reqBody = ""
) {
  let init;

  switch (reqMethod) {
    case "GET":
      init = {
        method: "GET"
      };
      break;

    case "POST":
      if (headerParams.length != 0) {
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
        init = {
          method: "POST"
        };
      }
  }

  try {
    return await fetch(pathUrl, init);
  } catch (error) {
    return console.log(error);
  }
}

//Función que captura el valor de una variable del query string
function getQueryVariable(variable) {
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

//Comprueba que el email sigue las pautas xxx@xxx.xxx
function emailIsValid(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

//-----------------------------------------------------------------------------------

//LLamado a las funciones cuando carga la ventana
window.onload = function() {
  gamesInfo();
  fetchByQueryString();
  if (!window.location.search) {
  }
};
