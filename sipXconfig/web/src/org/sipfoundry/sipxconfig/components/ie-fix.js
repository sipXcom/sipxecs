function iOS() {
    if (navigator.userAgent.match(/ipad|iphone|ipod/i)) {
        return true
    } else {
        return false
    }
}

function doNothing () {
}

function hoverToOverForIE() {
    if ((typeof document.body.style.maxHeight != "undefined") && (iOS() == false)) {
        // see: http://ajaxian.com/archives/detecting-ie7-in-javascript
        // modern browser: no need to simulate li:hover
        return;
    }

    function addOver() {
        this.className += " over";
    }

    function removeOver() {
        this.className = this.className.replace(" over", "");
    }

    if (document.getElementById) {
        var navRoot = document.getElementById("nav");
        for ( var i = 0; i < navRoot.childNodes.length; i++) {
            var node = navRoot.childNodes[i];
            if (node.nodeName == "LI") {
                if (iOS() == false) {
                    node.onmouseover = addOver;
                    node.onmouseout = removeOver;
                } else {
                    node.onclick = doNothing;
                }
            }
        }
    }
}
