//Variación JQuery de pedidos

// $('#registerBtn').click(evt => {
//   evt.preventDefault();
//   $.post(
//     '/api/players',
//     {
//       userName: $('input[name = userName]').val(),
//       password: $('input[name = password]').val()
//     },
//     (data, status) => {
//       console.log('Registration: ' + status);
//     }
//   ).fail(() => {
//     console.log('Registration unsuccesful');
//   });
// });

// $('#signInBtn').click(evt => {
//   evt.preventDefault();
//   $.post(
//     '/api/login',
//     {
//       userName: $('input[name = userName]').val(),
//       password: $('input[name = password]').val()
//     },
//     (data, status) => {
//       console.log('Log In: ' + status);
//     }
//   ).fail(function() {
//     console.log('Log in unsuccesful');
//   });
// });

// $('#logOutBtn').click(evt => {
//   evt.preventDefault();
//   $.post('/api/logout', {}, (data, status) => {
//     console.log('Log Out: ' + status);
//   }).fail(() => {
//     console.log('Logged Out Wrong');
//   });
// });

//Fetch y creación de leaderboard

//Desde Games Call
//Almacenaje de datos para pruebas
let leaderboardDataFromLeaderboardCall = [];

function leaderboardFromGamesRequest(info) {
  leaderboardDataFromLeaderboardCall = collectData(info);
  createLeaderboard1(leaderboardDataFromLeaderboardCall);
}

//Función que desglosa el /games data y devuelve objetos per player
//Magia pura
function collectData(info) {
  let playersArray = [];

  info.games.forEach(game =>
    game.game_players.forEach(gp => {
      let playerObj;

      playerObj = Object.fromEntries([
        ['id', gp.player.id],
        ['email', gp.player.email],
        ['scores', gp.score != null ? [gp.score] : []]
      ]);

      if (playersArray.some(player => player.id == playerObj.id)) {
        if (playerObj.scores.length != 0) {
          playersArray
            .filter(player => player.id == playerObj.id)[0]
            .scores.push(playerObj.scores[0]);
        }
      } else {
        playersArray.push(playerObj);
      }
    })
  );

  return playersArray;
}

//Función para generar la tabla de posiciones según el objeto extraído de /games
function createLeaderboard1(info) {
  let tableBody = document.getElementById('games-table');

  info.forEach(player => {
    if (player.scores.length != 0) {
      let tRow = document.createElement('tr');

      let nameCell = document.createElement('td');
      nameCell.innerText = player.email;

      let totalCell = document.createElement('td');
      totalCell.innerText = player.scores.reduce((prev, act) => prev + act, 0);

      let winCell = document.createElement('td');
      winCell.innerText = player.scores.reduce(
        (cont, act) => cont + counterParam(act, 1),
        0
      );

      let looseCell = document.createElement('td');
      looseCell.innerText = player.scores.reduce(
        (cont, act) => cont + counterParam(act, 0),
        0
      );

      let tieCell = document.createElement('td');
      tieCell.innerText = player.scores.reduce(
        (cont, act) => cont + counterParam(act, 0.5),
        0
      );

      let cells = [nameCell, totalCell, winCell, looseCell, tieCell];

      tRow.append(...cells);

      tableBody.appendChild(tRow);
    }
  });
}

function counterParam(score, value) {
  return score == value ? 1 : 0;
}

//Función que devuelve un objeto key-value del query string
function paramObj(search) {
  var obj = {};
  var reg = /(?:[?&]([^?&#=]+)(?:=([^&#]*))?)(?:#.*)?/g;

  search.replace(reg, function(match, param, val) {
    obj[decodeURIComponent(param)] =
      val === undefined ? '' : decodeURIComponent(val);
  });

  return obj;
}

// //Extra
// //Pedido de GP ids para menu select
// //Usa un fetch a una ruta no pedida en el task
// let gamePlayerIdsSelect = document.getElementById('gpSelect');

// function fetchIdsList() {
//   fetch('/api/gamePlayerIds')
//     .then(response => response.json())
//     .then(data => menuOptions(gamePlayerIdsSelect, data));
// }

// //Función creadora de opciones, necesita un target y array de opciones
// function menuOptions(menu, source) {
//   source.forEach(opt => {
//     let option = document.createElement('option');
//     option.innerText = opt;
//     option.setAttribute('value', opt);

//     menu.appendChild(option);
//   });
// }
