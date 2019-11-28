/* eslint-disable no-unused-vars */
/* eslint-disable no-console */

// Drag'n Droppin
// Ship placement madness

//contenedor para barcos ubicados
var shipsForPost = [];

//Variable necesaria para transportar el ID a los eventos donde no llega DataTransfer
let draggedItemId = "";

//Necesario para pasar info de posiciones de barco durante el drag
let shipLocations = [];

//Ubicacion temporal del barco movido en caso q el destino no sea valido
let provisoryShip = [];

/* Eventos sobre elemento arrastrado */
document.addEventListener("drag", drag);

function drag(event) {
  let ship = event.target;
  ship.classList.add("hide");
}

document.addEventListener("dragstart", dragstart);

function dragstart(event) {
  //referencia de elemento arrastrado, no todos los eventListener tienen acceso
  event.dataTransfer.setData("shipId", event.target.id);
  event.dataTransfer.effectAllowed = "move";

  draggedItemId = event.dataTransfer.getData("shipId");

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
}

document.addEventListener("dragend", dragend);

function dragend(event) {
  let ship = event.target;

  ship.classList.remove("hide");
}

/*Eventos sobre el contenedor destino*/
//efecto permitido del contenedor destino drop/no drop
document.addEventListener("dragover", dragover);

function dragover(event) {
  let cell = event.target;
  if (cell.classList.contains("wah")) {
    // prevent default to allow drop
    event.preventDefault();
    event.dataTransfer.effectAllowed = "move";
  } else {
    event.preventDefault();
    event.dataTransfer.effectAllowed = "none";
  }
}

//Lógica de mostrar posiciones permitidas
document.addEventListener("dragenter", dragenter);

function dragenter(event) {
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
}

document.addEventListener("dragleave", dragleave);

function dragleave(event) {
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
}

document.addEventListener("drop", drop);

function drop(event) {
  let cell = event.target;
  let ship = document.getElementById(event.dataTransfer.getData("shipId"));

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
}

//Si hay espacio suficiente, devuelve true y un array de posiciones del barco
function fits(id, array) {
  switch (id) {
    case "carrier":
      return array.length >= 5 ? {
        fits: true,
        positions: array.slice(0, 5)
      } : {
        fits: false,
        positions: array
      };
    case "battleship":
      return array.length >= 4 ? {
        fits: true,
        positions: array.slice(0, 4)
      } : {
        fits: false,
        positions: array
      };
    case "destroyer":
      return array.length >= 3 ? {
        fits: true,
        positions: array.slice(0, 3)
      } : {
        fits: false,
        positions: array
      };
    case "submarine":
      return array.length >= 3 ? {
        fits: true,
        positions: array.slice(0, 3)
      } : {
        fits: false,
        positions: array
      };
    case "patrol_boat":
      return array.length >= 2 ? {
        fits: true,
        positions: array.slice(0, 2)
      } : {
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
  let btn = document.createElement("a");
  btn.setAttribute("class", "rotate-btn btn btn-floating");
  btn.setAttribute("onclick", `rotateShip("${imgId}")`);

  btn.innerHTML = `<i class="btn-img tiny material-icons">loop</i>`;

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

  let posibleSpace = availableSpaceTakingIntoAccountShipRotation(cell, imgId);

  let posibleData = fits(imgId, posibleSpace);

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

//Array contenedor de salvoes
var salvoesForPost = [];

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
  }
}

// Agrega el event listener on click a la grilla
function salvoesListeners() {
  let battleGrid = Array.from(document.getElementsByClassName("battle-square"));

  battleGrid.forEach(square =>
    square.addEventListener("click", addSalvoToList)
  );
}