// roadmap.js - Safe and fully functional

// Initialize global variables
let currentStream = 'medical';
let zoomLevel = 1;

// Career roadmap data (shortened example, include your full data)
const careerRoadmaps = {
    medical: `
        graph TD
            A[High School - Medical Stream] --> B[NEET Preparation]
            B --> D[MBBS - Bachelor of Medicine]
            B --> E[BDS - Bachelor of Dental Surgery]
            style A fill:#e1f5fe
            style D fill:#c8e6c9
            style E fill:#c8e6c9
    `,
    'my-custom-flowchart': `
        graph TD
            A[Start] --> B{Decision}
            B -->|Yes| C[Path 1]
            B -->|No| D[Path 2]
            style A fill:#e1f5fe
            style C fill:#c8e6c9
            style D fill:#c8e6c9
    `
};

// Career info data (example, extend as needed)
const careerInfo = {
    'MBBS - Bachelor of Medicine': {
        title: 'Bachelor of Medicine and Bachelor of Surgery',
        description: 'A professional degree in medicine.',
        duration: '5.5 years',
        eligibility: '10+2 with PCB',
        entrance: 'NEET',
        career: 'Doctor, Surgeon',
        salary: '₹6-15 LPA'
    }
};

// Wait for DOM content to be fully loaded
document.addEventListener('DOMContentLoaded', () => {
    const diagramContainer = document.getElementById('roadmap-diagram');
    if (!diagramContainer) {
        console.error('Error: #roadmap-diagram element not found.');
        return;
    }

    // Initialize Mermaid
    mermaid.initialize({
        startOnLoad: false,
        theme: 'default',
        flowchart: { useMaxWidth: true, htmlLabels: true, curve: 'basis' }
    });

    // Initial render
    renderRoadmap(currentStream);

    // Setup stream selection
    document.querySelectorAll('.stream-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const stream = btn.dataset.stream;
            selectStream(stream);
        });
    });

    // Setup controls
    document.getElementById('zoomIn').addEventListener('click', zoomIn);
    document.getElementById('zoomOut').addEventListener('click', zoomOut);
    document.getElementById('resetZoom').addEventListener('click', resetZoom);
    document.getElementById('downloadSvg').addEventListener('click', downloadSvg);
    document.getElementById('closePanel').addEventListener('click', closeCareerPanel);

    // Close panel on overlay click
    document.querySelector('.overlay').addEventListener('click', closeCareerPanel);

    // Re-render on window resize
    window.addEventListener('resize', () => renderRoadmap(currentStream));
});

// Stream selection
function selectStream(stream) {
    currentStream = stream;
    document.querySelectorAll('.stream-btn').forEach(btn => btn.classList.remove('active'));
    const activeBtn = document.querySelector(`[data-stream="${stream}"]`);
    if (activeBtn) activeBtn.classList.add('active');
    renderRoadmap(stream);
}

// Render Mermaid diagram
async function renderRoadmap(stream) {
    const diagramContainer = document.getElementById('roadmap-diagram');
    if (!diagramContainer) return;

    diagramContainer.innerHTML = '<div class="loading">Loading roadmap...</div>';

    try {
        const diagramDefinition = careerRoadmaps[stream];
        if (!diagramDefinition) {
            diagramContainer.innerHTML = '<div class="error">No roadmap found for this stream.</div>';
            return;
        }

        const { svg } = await mermaid.render('roadmap-diagram', diagramDefinition);
        diagramContainer.innerHTML = svg;

        // Apply zoom
        applyZoom();

        // Make nodes clickable
        addNodeClickEvents();
    } catch (err) {
        console.error('Error rendering roadmap:', err);
        diagramContainer.innerHTML = '<div class="error">Error loading roadmap.</div>';
    }
}

// Add click events to nodes
function addNodeClickEvents() {
    document.querySelectorAll('.mermaid .node').forEach(node => {
        node.style.cursor = 'pointer';
        node.addEventListener('click', () => {
            const nodeText = node.querySelector('.label')?.textContent?.trim();
            if (nodeText && careerInfo[nodeText]) showCareerInfo(nodeText);
        });
    });
}

// Show career info panel
function showCareerInfo(title) {
    const info = careerInfo[title];
    if (!info) return;

    const panel = document.getElementById('careerInfoPanel');
    const titleEl = document.getElementById('careerTitle');
    const details = document.getElementById('careerDetails');

    titleEl.textContent = info.title;
    details.innerHTML = `
        <div class="career-detail"><strong>Description:</strong> ${info.description}</div>
        <div class="career-detail"><strong>Duration:</strong> ${info.duration}</div>
        <div class="career-detail"><strong>Eligibility:</strong> ${info.eligibility}</div>
        <div class="career-detail"><strong>Entrance:</strong> ${info.entrance}</div>
        <div class="career-detail"><strong>Career Options:</strong> ${info.career}</div>
        <div class="career-detail"><strong>Salary:</strong> ${info.salary}</div>
    `;

    document.querySelector('.overlay').classList.add('active');
    panel.style.display = 'block';
}

// Close info panel
function closeCareerPanel() {
    const panel = document.getElementById('careerInfoPanel');
    panel.style.display = 'none';
    document.querySelector('.overlay').classList.remove('active');
}

// Zoom functions
function zoomIn() { zoomLevel = Math.min(zoomLevel * 1.2, 3); applyZoom(); }
function zoomOut() { zoomLevel = Math.max(zoomLevel / 1.2, 0.5); applyZoom(); }
function resetZoom() { zoomLevel = 1; applyZoom(); }
function applyZoom() {
    const container = document.querySelector('.mermaid-container');
    if (container) container.style.transform = `scale(${zoomLevel})`;
}

// Download SVG
function downloadSvg() {
    const svgElement = document.querySelector('.mermaid svg');
    if (!svgElement) return;

    const svgData = new XMLSerializer().serializeToString(svgElement);
    const blob = new Blob([svgData], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `career-roadmap-${currentStream}.svg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
}
