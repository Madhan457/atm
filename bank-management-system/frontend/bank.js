/* ==========================================================================
   Trust Bank - Customer Bank Dashboard Controller (bank.js)
   ========================================================================== */

let currentCustomer = null;

document.addEventListener('DOMContentLoaded', () => {
    initBankDashboard();
});

/**
 * Initialize Bank Dashboard Page
 */
async function initBankDashboard() {
    currentCustomer = checkAuthGuard();
    if (!currentCustomer) return;

    // Render initial cached info
    renderCustomerInfo(currentCustomer);

    // Fetch fresh profile data from backend API
    await fetchLatestProfile();
}

/**
 * Fetch latest customer profile from GET /api/customers/profile
 */
async function fetchLatestProfile() {
    if (!currentCustomer) return;
    if (!currentCustomer.id) {
        showToast('Customer session is missing its database ID. Please log in again.', 'error');
        return;
    }

    try {
        const { result } = await requestApi(`${API_BASE_URL}/api/customers/profile`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Customer-Id': currentCustomer.id
            }
        });

        if (result.success && result.customer) {
            currentCustomer = result.customer;
            localStorage.setItem('apex_user', JSON.stringify(currentCustomer));
            renderCustomerInfo(currentCustomer);
        } else {
            throw new Error(result.message || 'The backend did not return customer details.');
        }
    } catch (error) {
        console.error('Customer profile refresh failed:', error);
        showToast(`Could not refresh account details: ${error.message}`, 'error');
    }
}

/**
 * Render Customer Info into DOM elements
 */
function renderCustomerInfo(customer) {
    if (!customer) return;

    const navGreeting = document.getElementById('nav-user-greeting');
    if (navGreeting) navGreeting.textContent = `Welcome, ${customer.fullName}`;

    document.getElementById('dash-welcome').textContent = `Welcome, ${customer.fullName}`;
    document.getElementById('dash-account-type').textContent = customer.accountType || 'Savings';

    // Format Currency (₹10,000.00 / $10,000.00)
    const balanceNum = Number(customer.balance);
    const formattedBalance = Number.isFinite(balanceNum)
        ? `₹${balanceNum.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
        : 'Unavailable';
    document.getElementById('dash-balance').textContent = formattedBalance;

    document.getElementById('dash-account-number').textContent = customer.accountNumber || 'Unavailable';
    document.getElementById('dash-fullname').textContent = customer.fullName;
    document.getElementById('dash-username').textContent = customer.username;
    document.getElementById('dash-email').textContent = customer.email;
    document.getElementById('dash-phone').textContent = customer.phone;
    document.getElementById('dash-dob').textContent = customer.dateOfBirth || '-';
    document.getElementById('dash-address').textContent = customer.address;

    if (customer.createdAt) {
        const dateObj = new Date(customer.createdAt);
        document.getElementById('dash-created-at').textContent = dateObj.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    } else {
        document.getElementById('dash-created-at').textContent = 'Today';
    }
}

/**
 * Show / Hide Update Profile Modal
 */
function showUpdateModal() {
    if (!currentCustomer) return;

    document.getElementById('update-fullname').value = currentCustomer.fullName || '';
    document.getElementById('update-email').value = currentCustomer.email || '';
    document.getElementById('update-phone').value = currentCustomer.phone || '';
    document.getElementById('update-address').value = currentCustomer.address || '';

    const modal = document.getElementById('update-modal');
    if (modal) modal.classList.remove('hidden');
}

function hideUpdateModal() {
    const modal = document.getElementById('update-modal');
    if (modal) modal.classList.add('hidden');
}

/**
 * Submit Profile Update (PUT /api/customers/profile)
 */
async function handleProfileUpdateSubmit(event) {
    event.preventDefault();

    if (!currentCustomer || !currentCustomer.id) return;

    const fullName = document.getElementById('update-fullname').value.trim();
    const email = document.getElementById('update-email').value.trim();
    const phone = document.getElementById('update-phone').value.trim();
    const address = document.getElementById('update-address').value.trim();

    const payload = { fullName, email, phone, address };

    try {
        const { result } = await requestApi(`${API_BASE_URL}/api/customers/profile`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'X-Customer-Id': currentCustomer.id
            },
            body: JSON.stringify(payload)
        });

        if (result.success && result.customer) {
            currentCustomer = result.customer;
            localStorage.setItem('apex_user', JSON.stringify(currentCustomer));
            renderCustomerInfo(currentCustomer);
            hideUpdateModal();
            showToast('Profile updated successfully!', 'success');
        } else {
            showToast(result.message || 'Failed to update profile.', 'error');
        }
    } catch (error) {
        showToast(error.message, 'error');
    }
}

/**
 * Delete Account Modals & Handling
 */
function showDeleteConfirmationModal() {
    const modal = document.getElementById('delete-modal');
    if (modal) modal.classList.remove('hidden');
}

function hideDeleteConfirmationModal() {
    const modal = document.getElementById('delete-modal');
    if (modal) modal.classList.add('hidden');
}

async function confirmDeleteAccount() {
    if (!currentCustomer || !currentCustomer.id) return;

    try {
        const { result } = await requestApi(`${API_BASE_URL}/api/customers/account`, {
            method: 'DELETE',
            headers: {
                'X-Customer-Id': currentCustomer.id
            }
        });

        if (result.success) {
            hideDeleteConfirmationModal();
            showToast('Customer account deleted successfully.', 'success');
            
            // Clear session state and redirect to index.html
            localStorage.removeItem('apex_user');
            sessionStorage.removeItem('apex_user');

            setTimeout(() => {
                window.location.href = 'index.html';
            }, 800);
        } else {
            showToast(result.message || 'Failed to delete account.', 'error');
        }
    } catch (error) {
        showToast(error.message, 'error');
    }
}
