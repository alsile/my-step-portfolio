// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random quote to the page.
 */

window.onload = _ =>
  setDarkMode();

var curr = 0;

const quotes = [
  'pay forward what you can never pay back',
  'the longest relationship you will have with anyone is with yourself',
  'fear being capable of anything, and choosing to do nothing',
  'stay hydrated',
  'yerrrrrrrrr',
  'the most growth comes from the hardest experiences'
];

function nextQuote() {
  const quoteContainer = document.getElementById('quote-container');
  curr++;
  var use = curr;
  if (use < 0) {
    use = (use % quotes.length);
    if (use != 0) {
      use += quotes.length;
    }
  } else {
    use = use % quotes.length;
  }
  const quote = quotes[use];
  quoteContainer.innerText = quote;
}

function prevQuote() {
  const quoteContainer = document.getElementById('quote-container');
  curr--;
  var use = curr;
  if (use < 0) {
    use = (use % quotes.length);
    if (use != 0) {
      use += quotes.length;
    }
  } else {
    use = use % quotes.length;
  }
  const quote = quotes[use];
  quoteContainer.innerText = quote;
}

function setDarkMode() {
  if (localStorage.getItem("darkOn") === "true") {
    document.body.classList.add("dark-mode");
  } else {
    document.body.classList.remove("dark-mode");
  }
}

function toggleDarkMode() {
  if (localStorage.getItem("darkOn") === "true") {
    localStorage.darkOn = "false";
  } else {
    localStorage.darkOn = "true";
  }
  setDarkMode(localStorage.getItem("darkOn"));
}

async function consoleResponse() {
  const response = await fetch('/data');
  const comments = await response.json();
  consoleOutput = document.getElementById('console-output');

  if (comments.limit !== null) {
    document.getElementById('num-comments').placeholder = comments.limit;
  }

  var limit = Math.min(comments.limit, comments.jsonList.length);
  for (var i = 0; i < limit; i++) {
    consoleOutput.appendChild(createListElement(comments.jsonList[i]));
  }
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}