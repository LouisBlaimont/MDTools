// Shared variables
let resizing = null;
let startX = 0;
let startY = 0;
let startWidth = 0;
let startHeight = 0;

export function startResizeBoth(event, div) {
    event.preventDefault();

    resizing = div;
    startX = event.clientX;
    startY = event.clientY;
    startWidth = div.offsetWidth;
    startHeight = div.offsetHeight;

    document.addEventListener('mousemove', resizeBoth);
    document.addEventListener('mouseup', stopResizeBoth);
}

function resizeBoth(event) {
    if (!resizing) return;

    const deltaX = event.clientX - startX;
    const deltaY = event.clientY - startY;

    const newWidth = startWidth + deltaX;
    const newHeight = startHeight + deltaY;

    resizing.style.width = `${newWidth}px`;
    resizing.style.height = `${newHeight}px`;    
}

function stopResizeBoth() {
    document.removeEventListener('mousemove', resizeBoth);
    document.removeEventListener('mouseup', stopResizeBoth);
    resizing = null;
}

export function startResizeVertical(event, div) {
    event.preventDefault();

    resizing = div;
    startY = event.clientY;
    startHeight = div.offsetHeight;

    document.addEventListener('mousemove', resizeVertical);
    document.addEventListener('mouseup', stopResizeVertical);
}

function resizeVertical(event) {
    if (!resizing) return;

    const deltaY = event.clientY - startY;

    const newHeight = startHeight + deltaY;

    resizing.style.height = `${newHeight}px`;
}

function stopResizeVertical() {
    document.removeEventListener('mousemove', resizeVertical);
    document.removeEventListener('mouseup', stopResizeVertical);
    resizing = null;
}
