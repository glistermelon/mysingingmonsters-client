const githubRepo = "glistermelon/mysingingmonsters-client";

function buildGithubElement() {
    let a = document.createElement('a');
    a.style = "max-height: 20px; display: inline-block; position: relative;";
    a.href = 'https://github.com/' + githubRepo;
    let img = document.createElement('img');
    img.src = '/_static/github.svg';
    img.style = 'max-height: inherit; position: relative; margin-right: 8px';
    let span = document.createElement('span')
    span.textContent = githubRepo;
    a.appendChild(img);
    a.appendChild(span);
    return a;
}

var container = null;
var crumbs = null;
var githubElm = null;

document.addEventListener("DOMContentLoaded", () => {
    container = document.getElementsByClassName('wy-breadcrumbs')[0];
    crumbs = Array.prototype.slice.call(container.children);
    githubElm = buildGithubElement();
    crumbs[1].after(githubElm);
    resize();
});

function resize() {
    if (container == null || crumbs == null || githubElm == null) return;
    let space = container.offsetWidth - crumbs.map(e => e.offsetWidth).reduce((a, b) => a + b);
    if (space < githubElm.offsetWidth) {
        githubElm.style.setProperty('left', '');
    }
    else {
        let left = (space - githubElm.offsetWidth) / 2;
        githubElm.style.setProperty('left', `${left}px`);
    }
}

window.addEventListener('resize', resize, true);