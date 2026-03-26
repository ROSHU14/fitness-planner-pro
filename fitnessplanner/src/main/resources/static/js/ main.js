const API_BASE_URL = '/api';
let token = localStorage.getItem('token');

// ========== CHECK AUTH ==========
function checkAuth() {
    const publicPages = ['/index.html', '/login.html', '/signup.html'];
    const currentPage = window.location.pathname;
    if (!publicPages.includes(currentPage) && !token) {
        window.location.href = '/login.html';
        return false;
    }
    return true;
}

// ========== LOGOUT ==========
function logout() {
    localStorage.clear();
    window.location.href = '/index.html';
}

// ========== DARK MODE ==========
function toggleDarkMode() {
    if (document.documentElement.classList.contains('dark')) {
        document.documentElement.classList.remove('dark');
        localStorage.setItem('darkMode', 'false');
    } else {
        document.documentElement.classList.add('dark');
        localStorage.setItem('darkMode', 'true');
    }
}

function loadDarkModePreference() {
    if (localStorage.getItem('darkMode') === 'true') {
        document.documentElement.classList.add('dark');
    }
}

// ========== USER INFO ==========
function setUserInfo() {
    const userEmail = localStorage.getItem('userEmail');
    if (userEmail) {
        const name = userEmail.split('@')[0];
        const elements = document.querySelectorAll('#userName, #sidebarUserName, #welcomeName');
        elements.forEach(el => { if (el) el.textContent = name; });
    }
}

// ========== STREAK ==========
async function loadStreak() {
    try {
        token = localStorage.getItem('token');
        const res = await fetch(`${API_BASE_URL}/streak`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        if (data.success) {
            const elements = document.querySelectorAll('#streakCount, #streakDisplay');
            elements.forEach(el => { if (el) el.textContent = data.currentStreak; });
        }
    } catch (e) { console.error('Streak error:', e); }
}

// ========== WATER TRACKER ==========
async function loadWaterIntake() {
    try {
        token = localStorage.getItem('token');
        const res = await fetch(`${API_BASE_URL}/water/stats`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        if (data.success) {
            const liters = data.data.today;
            const percent = Math.min(100, (liters / 3) * 100);
            const waterEl = document.getElementById('waterLiters');
            const percentEl = document.getElementById('waterPercent');
            const barEl = document.getElementById('waterBar');
            if (waterEl) waterEl.textContent = liters.toFixed(1);
            if (percentEl) percentEl.textContent = Math.round(percent);
            if (barEl) barEl.style.width = percent + '%';
        }
    } catch (e) { console.error('Water error:', e); }
}

async function addWater(liters) {
    try {
        token = localStorage.getItem('token');
        await fetch(`${API_BASE_URL}/water/add`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify({ liters: liters })
        });
        loadWaterIntake();
        alert(`Added ${liters}L of water! 💧`);
    } catch (e) { alert('Error adding water'); }
}

// ========== WORKOUT ==========
async function logWorkout(type, name, duration) {
    try {
        token = localStorage.getItem('token');
        const res = await fetch(`${API_BASE_URL}/workouts/log`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify({ workoutType: type, exerciseName: name, durationMinutes: duration })
        });
        const data = await res.json();
        if (data.success) {
            alert(`${name} logged! 🎉`);
            loadStreak();
        } else {
            alert('Error logging workout');
        }
    } catch (e) { alert('Error logging workout'); }
}

// ========== MEAL ==========
async function logMeal(type) {
    const food = prompt('Food name:', 'Healthy Meal');
    if (!food) return;
    const calories = prompt('Calories:', '500');
    if (!calories) return;
    try {
        token = localStorage.getItem('token');
        await fetch(`${API_BASE_URL}/meals/log`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify({ mealType: type, foodName: food, quantity: 1, unit: 'serving', calories: parseInt(calories) })
        });
        alert(`${food} logged! 🍽️`);
    } catch (e) { alert('Error logging meal'); }
}

// ========== WEIGHT ==========
async function logWeight() {
    const weightInput = document.getElementById('weightInput');
    if (!weightInput) return;
    const weight = weightInput.value;
    if (!weight) { alert('Please enter weight'); return; }
    try {
        token = localStorage.getItem('token');
        const res = await fetch(`${API_BASE_URL}/progress/add`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
            body: JSON.stringify({ weight: parseFloat(weight) })
        });
        const data = await res.json();
        if (data.success) {
            alert('Weight logged! 📊');
            weightInput.value = '';
            loadWeightChart();
        } else { alert('Error logging weight'); }
    } catch (e) { alert('Error logging weight'); }
}

// ========== WEIGHT CHART ==========
async function loadWeightChart() {
    try {
        token = localStorage.getItem('token');
        const res = await fetch(`${API_BASE_URL}/progress/chart`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        const chartCanvas = document.getElementById('weightChart');
        const noDataMsg = document.getElementById('noWeightData');
        if (data.success && data.data && data.data.length > 0) {
            if (chartCanvas) {
                chartCanvas.classList.remove('hidden');
                if (noDataMsg) noDataMsg.classList.add('hidden');
                const dates = data.data.map(p => p.date);
                const weights = data.data.map(p => p.weight);
                const ctx = chartCanvas.getContext('2d');
                if (window.weightChart) window.weightChart.destroy();
                window.weightChart = new Chart(ctx, {
                    type: 'line',
                    data: { labels: dates, datasets: [{ label: 'Weight (kg)', data: weights, borderColor: 'rgb(139, 92, 246)', backgroundColor: 'rgba(139, 92, 246, 0.1)', tension: 0.4, fill: true }] },
                    options: { responsive: true, maintainAspectRatio: false }
                });
            }
        } else {
            if (chartCanvas) chartCanvas.classList.add('hidden');
            if (noDataMsg) noDataMsg.classList.remove('hidden');
        }
    } catch (e) { console.error('Chart error:', e); }
}

// ========== DASHBOARD STATS ==========
async function loadDashboardStats() {
    try {
        token = localStorage.getItem('token');
        const res = await fetch(`${API_BASE_URL}/fitness/plan`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        if (data.success) {
            const plan = data.data;
            const bmrEl = document.getElementById('bmrValue');
            const dailyEl = document.getElementById('dailyCalories');
            const targetEl = document.getElementById('targetCalories');
            if (bmrEl) bmrEl.textContent = Math.round(plan.bmr);
            if (dailyEl) dailyEl.textContent = Math.round(plan.dailyCalories);
            if (targetEl) targetEl.textContent = Math.round(plan.targetCalories);
            const goalBadge = document.getElementById('goalBadge');
            if (goalBadge) {
                const emoji = plan.weightGoal.includes('Loss') ? '🏃‍♂️' : (plan.weightGoal.includes('Gain') ? '💪' : '⚖️');
                goalBadge.innerHTML = emoji + ' ' + plan.weightGoal;
            }
            const goalDesc = document.getElementById('goalDescription');
            if (goalDesc) goalDesc.textContent = plan.goalDescription;
        }
    } catch (e) { console.error('Dashboard error:', e); }
}

// ========== EXERCISES ==========
async function loadExercises() {
    try {
        const res = await fetch(`${API_BASE_URL}/exercises`);
        const data = await res.json();
        const container = document.getElementById('exercisesContainer');
        if (container && data.success && data.data && data.data.length > 0) {
            let html = '';
            for (let i = 0; i < data.data.length; i++) {
                const ex = data.data[i];
                html += '<div class="border rounded-lg p-4 hover:shadow-lg transition bg-white dark:bg-gray-800">';
                html += '<h4 class="font-bold text-purple-600 text-lg">' + ex.name + '</h4>';
                html += '<p class="text-sm text-gray-500 dark:text-gray-400">' + ex.category + ' | ' + ex.equipment + '</p>';
                html += '<div class="bg-gray-100 dark:bg-gray-700 rounded-lg my-3">';
                html += '<iframe width="100%" height="180" src="' + ex.videoUrl + '" frameborder="0" allowfullscreen class="rounded-lg"></iframe>';
                html += '</div>';
                html += '<button onclick="logWorkout(\'Strength\', \'' + ex.name + '\', 30)" class="mt-3 w-full bg-purple-600 text-white py-2 rounded-lg hover:bg-purple-700 transition">📝 Log This Workout</button>';
                html += '</div>';
            }
            container.innerHTML = html;
        } else if (container) {
            container.innerHTML = '<p class="text-gray-500">No exercises found. Run init first.</p>';
        }
    } catch (e) { console.error('Exercises error:', e); }
}

// ========== INITIALIZE ==========
function initializePage() {
    token = localStorage.getItem('token');
    if (!checkAuth()) return;
    setUserInfo();
    loadDarkModePreference();
    loadStreak();
    loadWaterIntake();
    loadDashboardStats();
}

// ========== RUN ON PAGE LOAD ==========
document.addEventListener('DOMContentLoaded', function() {
    const publicPages = ['/index.html', '/login.html', '/signup.html'];
    const currentPage = window.location.pathname;
    if (!publicPages.includes(currentPage) && !localStorage.getItem('token')) {
        window.location.href = '/login.html';
        return;
    }
    initializePage();
    if (window.location.pathname === '/progress.html') {
        loadWeightChart();
    }
    if (window.location.pathname === '/workouts.html') {
        loadExercises();
    }
    if (window.location.pathname === '/dashboard.html') {
        loadWeightChart();
    }
});