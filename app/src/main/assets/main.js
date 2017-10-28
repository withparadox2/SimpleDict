var backStack = [];

function setContent(text) {
    var container = getContainer();
    saveToStack(container);
    container.innerHTML = text;
    window.scrollTo(0, 0);
    var wordItems = container.getElementsByClassName('word-item') || [];
    Array.prototype.forEach.call(wordItems, function(word) {
        var header = word.childNodes[0];
        var detail = word.childNodes[1];

        header.onclick = function() {
            toggleShow(detail, header);
        }
    });
}

function saveToStack(container) {
    if (container && container.childNodes && container.childNodes.length > 0) {
        backStack.push({
            value: container.childNodes[0],
            position: window.scrollY
        })
    }
}

function restoreFromStack(container, item) {
    container.innerHTML = '';
    container.appendChild(item.value);
    window.scrollTo(0, item.position);
    currentShowWord = item.key;
}

function getContainer() {
    return document.getElementById('top-container');
}

function backClick() {
    if (backStack.length == 0) {
        return false;
    }

    restoreFromStack(getContainer(), backStack.pop())
}

function clearBackStack() {
    backStack = [];
}

function toggleShow(ele, header) {
    if (!ele.style) {
        ele.style = {};
    }
    var display = ele.style.display;
    var isHideContent = true;

    if (!display || display == 'block') {
        ele.style.display = 'none';
    } else {
        isHideContent = false;
        ele.style.display = 'block';
    }

    var arrowNode = header.childNodes[1];
    if (arrowNode) {
        arrowNode.className = isHideContent ? 'arrow-right' : 'arrow-down';
    }
}

function checkRange(range, x, y) {
    var rects = range.getClientRects();

    for (var i = 0; i != rects.length; i++) {
        var rect = rects[i];

        if (rect.left <= x && rect.right >= x &&
            rect.top <= y && rect.bottom >= y) {
            range.expand("word");
            var ret;

            var fragment = range.cloneContents();
            var stresses = fragment.querySelectorAll('.dsl_st');

            if (stresses.length > 0) {
                for (var i = 0; i < stresses.length; ++i) {
                    stresses[i].textContent = '';
                }
                ret = fragment.textContent;
            } else {
                ret = range.toString();
            }

            var subs = ret.split(' ');
            var k = 0;

            for (; k < subs.length && subs[k].length == 0; k++) {}

            ret = subs[k];

            range.detach();
            return ret;
        }
    }

    return null;
}

function getWordAtPoint(elem, x, y) {
    if (elem.nodeType == elem.ELEMENT_NODE) {
        if (elem.nodeName == 'A' || elem.getAttribute('class') == 'dict-title') {
            return null;
        }
    }

    if (elem.nodeType == elem.TEXT_NODE) {
        var range = elem.ownerDocument.createRange();
        range.selectNodeContents(elem);
        var currentPos = 0;
        var endPos = range.endOffset;
        var wordStartPos = 0;
        var start = false;

        while (currentPos < endPos) {
            range.setStart(elem, currentPos);
            range.setEnd(elem, currentPos + 1);

            var symb = range.toString();
            if (symb != ' ' && symb != '\n' && symb != '\r' && symb != '\t' && symb != '\xa0') {
                if (!start) {
                    wordStartPos = currentPos;
                    start = true;
                }
            } else {
                if (start) {
                    range.setStart(elem, wordStartPos);
                    range.setEnd(elem, currentPos);
                    start = false;

                    var ret = checkRange(range, x, y);
                    if (ret != null) {
                        return (ret);
                    }
                }
            }
            currentPos += 1;
        }

        if (start) {
            range.setStart(elem, wordStartPos);
            range.setEnd(elem, currentPos);

            var ret = checkRange(range, x, y);
            if (ret) {
                return (ret);
            }
        }
    } else {
        for (var i = 0; i < elem.childNodes.length; i++) {
            var range = elem.childNodes[i].ownerDocument.createRange();
            range.selectNodeContents(elem.childNodes[i]);

            if (range.getBoundingClientRect() == null) {
                range.detach();
                continue;
            }

            if (range.getBoundingClientRect().left <= x && range.getBoundingClientRect().right >= x &&
                range.getBoundingClientRect().top <= y && range.getBoundingClientRect().bottom >= y) {
                range.detach();
                var result = getWordAtPoint(elem.childNodes[i], x, y);

                if (result != null) {
                    return result;
                }
            } else {
                range.detach();
            }
        }
    }

    return null;
}

function handleWordClick(event) {
    // Make sure this is not a child element of a link
    for (var elem = event.target; elem; elem = elem.parentNode) {
        if (elem.nodeName == 'A') {
            return;
        }
    }
    var word = getWordAtPoint(event.target, event.x, event.y);
    if (word) {
        if (window.jsi && window.jsi.onClickWord) {
            window.jsi.onClickWord(word);
        } else {
            alert(word);
        }
    }
}

//Steal from golden dict
window.addEventListener('click', handleWordClick, false);