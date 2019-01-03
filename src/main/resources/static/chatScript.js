    function onLoad() {
        var ws = "ws://localhost:4242/chat"
        websocket = new WebSocket(ws);
        websocket.onopen = function (ev) {
            onOpen(ev)
        };
        websocket.onmessage =  function (ev) {
            onMessage(ev);
        }
    }

    function onOpen(ev) {
        console.log("Połączono");
    }

    function onMessage(ev) {
        var message = ev.data;

        messages.innerHTML = messages.innerHTML + "<li class = \"message\">" + message + "</li>";
        messages.scrollTop = messages.scrollHeight;

// messages.innerHTML - zawiera wszystko cały HTML z id="messages"
// trzyma wiadomości z html + dodawanie nowych
// + li message - aktualna wiadomość
// scrollTop = scrollHeight  - automatyczne scrollowanie chatu do góry
    }
    function sendMessage() {
        var message = writer.value;
        writer.value = "";
        websocket.send(message);
    }