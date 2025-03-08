export function startResize(event, div) {
    resizing = div;
    startX = event.clientX;
    startY = event.clientY;
    startWidth = div.offsetWidth;
    startHeight = div.offsetHeight;
    document.addEventListener('mousemove', resize);
    document.addEventListener('mouseup', stopResize);
}

export function resize(event) {
    if (!resizing) return;
    const deltaX = event.clientX - startX;
    const deltaY = event.clientY - startY;
    resizing.style.width = `${startWidth + deltaX}px`;
    resizing.style.height = `${startHeight + deltaY}px`;
}

export function stopResize() {
    resizing = null;
    document.removeEventListener('mousemove', resize);
    document.removeEventListener('mouseup', stopResize);
}