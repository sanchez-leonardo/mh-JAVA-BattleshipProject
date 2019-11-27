//Formulario de registro y entrada

let multiForm = document.getElementById('multiForm');

let registerBtn = document.getElementById('registerBtn');

let signInBtn = document.getElementById('signInBtn');

//register fetch
function registerUser() {
  if (
    emailIsValid(multiForm.userName.value) &&
    multiForm.password.value != ''
  ) {
    let formFields = new URLSearchParams({
      userName: multiForm.userName.value,
      password: multiForm.password.value
    });

    customFetch(
      'POST',
      '/api/players',
      [{ 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' }],
      formFields
    )
      .then(data => {
        if (data.ok) {
          signInUser();
        }
      })
      .catch(error => console.log(error));
  } else {
    alert('Email must contain one "@" and a valid domain name');
  }
}

//Sign In fetch
function signInUser() {
  if (
    emailIsValid(multiForm.userName.value) &&
    multiForm.password.value != ''
  ) {
    let formFields = new URLSearchParams({
      userName: multiForm.userName.value,
      password: multiForm.password.value
    });

    customFetch(
      'POST',
      '/api/login',
      [{ 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' }],
      formFields
    )
      .then(data => {
        if (data.ok) {
          multiForm.reset();

          window.location = '/web/games.html';
        }
      })
      .catch(error => console.log(error));
  } else {
    alert('User name is invalid or Password are invalid');
  }
}

multiForm.addEventListener('submit', evt => evt.preventDefault());

registerBtn.addEventListener('click', registerUser);

signInBtn.addEventListener('click', signInUser);

//-------------------------------------------------------------------------------------------------

//Generación de la tabla de leaderboard desde la llamada a /leaderboard
//Variables para testeo
let leaderboardCallData;

function leaderboardFromLeaderboardRequest() {
  fetch('/api/leaderboard')
    .then(response => response.json())
    .then(data => {
      leaderboardCallData = data;
      createLeaderboard2(data);
    });
}

//Crea una tabla de posiciones segun el llamado a /leaderboard
function createLeaderboard2(data) {
  let tableBody = document.getElementById('leaderboard-table');

  data.forEach(player => {
    if (player.scores.matches != 0) {
      let tRow = document.createElement('tr');

      let nameCell = document.createElement('td');
      nameCell.innerText = player.email;

      let totalCell = document.createElement('td');
      totalCell.innerText = player.scores.total;

      let winCell = document.createElement('td');
      winCell.innerText = player.scores.win;

      let looseCell = document.createElement('td');
      looseCell.innerText = player.scores.loose;

      let tieCell = document.createElement('td');
      tieCell.innerText = player.scores.tie;

      let cells = [nameCell, totalCell, winCell, looseCell, tieCell];

      tRow.append(...cells);

      tableBody.appendChild(tRow);
    }
  });
}

//Utilities

// Funcion generica de fetch con constuccion de header
async function customFetch(
  reqMethod,
  pathUrl,
  headerParams = [],
  reqBody = ''
) {
  let init;

  switch (reqMethod) {
    case 'GET':
      init = { method: 'GET' };
      break;

    case 'POST':
      if (headerParams.length !== 0) {
        let headerObj = new Headers();
        headerParams.forEach(header =>
          headerObj.set(Object.keys(header)[0], Object.values(header)[0])
        );

        init = {
          method: 'POST',
          headers: headerObj,
          body: reqBody
        };
      } else {
        init = { method: 'POST' };
      }
  }

  try {
    return await fetch(pathUrl, init);
  } catch (error) {
    return console.log(error);
  }
}

//Comprueba que el email sigue las pautas xxx@xxx.xxx
function emailIsValid(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

//-----------------------------------------------------------------------------------

//LLamado a las funciones cuando carga la ventana
window.onload = function() {
  leaderboardFromLeaderboardRequest();
};
