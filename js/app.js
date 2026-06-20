// المتغيرات العامة
let accounts = [];
let currentEditId = null;
let currentSearchTerm = '';
let currentFilterType = 'all';
let currentSortBy = 'date';

// تصنيفات افتراضية وأسماؤها المعروضة
const CATEGORY_LABELS = {
    personal: 'شخصي',
    food: 'طعام',
    transport: 'مواصلات',
    shopping: 'تسوق',
    bills: 'فواتير',
    other: 'أخرى'
};

// رموز العملات
const CURRENCY_SYMBOLS = {
    'ريال سعودي': 'ر.س',
    'درهم إماراتي': 'د.إ',
    'دينار كويتي': 'د.ك',
    'دولار أمريكي': '$',
    'يورو': '€'
};

// تحميل البيانات عند بدء التطبيق
document.addEventListener('DOMContentLoaded', function() {
    loadData();

    if (document.getElementById('account-category')) {
        loadCategories();
    }

    if (document.getElementById('accounts-list')) {
        renderAccounts();
        updateSummary();
    }

    // تعيين تاريخ اليوم كقيمة افتراضية
    const dateInput = document.getElementById('account-date');
    if (dateInput) {
        dateInput.valueAsDate = new Date();
    }

    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            currentSearchTerm = this.value.trim();
            renderAccounts();
        });
    }

    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            filterAccounts(this.dataset.filter);
        });
    });

    const sortSelect = document.getElementById('sort-select');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            sortAccounts(this.value);
        });
    }
});

// تحميل البيانات من localStorage
function loadData() {
    const savedData = localStorage.getItem('accountingData');
    if (savedData) {
        accounts = JSON.parse(savedData);
    }
}

// حفظ البيانات في localStorage
function saveData() {
    localStorage.setItem('accountingData', JSON.stringify(accounts));
}

// حساب إجمالي مجموعة من المعاملات
function getGroupTotal(group) {
    return group.reduce((sum, acc) => acc.type === 'expense' ? sum - acc.amount : sum + acc.amount, 0);
}

// تصفية الحسابات حسب البحث ونوع المعاملة
function getFilteredAccounts() {
    return accounts.filter(account => {
        if (currentFilterType !== 'all' && account.type !== currentFilterType) {
            return false;
        }
        if (currentSearchTerm) {
            const term = currentSearchTerm.toLowerCase();
            const inName = account.name.toLowerCase().includes(term);
            const inNotes = (account.notes || '').toLowerCase().includes(term);
            if (!inName && !inNotes) return false;
        }
        return true;
    });
}

// عرض الحسابات
function renderAccounts() {
    const container = document.getElementById('accounts-list');
    if (!container) return;
    container.innerHTML = '';

    if (accounts.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 50px; color: #757575;">
                <i class="fas fa-inbox" style="font-size: 64px; margin-bottom: 20px;"></i>
                <p>لا توجد حسابات مسجلة</p>
                <p>اضغط على + لإضافة حساب جديد</p>
            </div>
        `;
        return;
    }

    const filteredAccounts = getFilteredAccounts();

    if (filteredAccounts.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 50px; color: #757575;">
                <i class="fas fa-search" style="font-size: 64px; margin-bottom: 20px;"></i>
                <p>لا توجد نتائج مطابقة</p>
            </div>
        `;
        return;
    }

    // تجميع الحسابات حسب الاسم
    const groupedAccounts = {};
    filteredAccounts.forEach(account => {
        if (!groupedAccounts[account.name]) {
            groupedAccounts[account.name] = [];
        }
        groupedAccounts[account.name].push(account);
    });

    const groupNames = Object.keys(groupedAccounts).sort((nameA, nameB) => {
        const groupA = groupedAccounts[nameA];
        const groupB = groupedAccounts[nameB];
        if (currentSortBy === 'name') {
            return nameA.localeCompare(nameB);
        }
        if (currentSortBy === 'amount') {
            return getGroupTotal(groupB) - getGroupTotal(groupA);
        }
        // الأحدث افتراضياً
        const latestA = groupA.reduce((latest, acc) => acc.date > latest ? acc.date : latest, groupA[0].date);
        const latestB = groupB.reduce((latest, acc) => acc.date > latest ? acc.date : latest, groupB[0].date);
        return latestB.localeCompare(latestA);
    });

    groupNames.forEach(name => {
        const accountGroup = groupedAccounts[name];
        const totalAmount = getGroupTotal(accountGroup);
        const count = accountGroup.length;

        const card = document.createElement('div');
        card.className = 'account-card';

        const iconClass = totalAmount >= 0 ? 'income' : 'expense';
        const arrowIcon = totalAmount >= 0 ? '▲' : '▼';
        const amountClass = totalAmount >= 0 ? 'positive' : 'negative';
        const amountText = formatAmount(Math.abs(totalAmount));

        card.innerHTML = `
            <div class="account-card-main">
                <div class="account-info">
                    <div class="account-icon ${iconClass}">
                        <i class="fas fa-user"></i>
                    </div>
                    <div class="account-details">
                        <h3></h3>
                        <div>
                            <span class="amount ${amountClass}">${arrowIcon} ${amountText}</span>
                            <span class="count">${count}</span>
                        </div>
                    </div>
                </div>
                <div class="account-actions">
                    <button class="btn-toggle-details" title="عرض التفاصيل"><i class="fas fa-chevron-down"></i></button>
                    <button class="btn-add-transaction" title="إضافة معاملة"><i class="fas fa-plus"></i></button>
                </div>
            </div>
        `;
        card.querySelector('h3').textContent = name;

        const detailsDiv = document.createElement('div');
        detailsDiv.className = 'transactions-detail';
        detailsDiv.style.display = 'none';

        const sortedGroup = [...accountGroup].sort((a, b) => b.date.localeCompare(a.date));
        sortedGroup.forEach(account => {
            const row = document.createElement('div');
            row.className = 'transaction-row';

            const info = document.createElement('div');
            info.className = 'transaction-info';

            const amountSpan = document.createElement('span');
            amountSpan.className = account.type === 'expense' ? 'negative' : 'positive';
            amountSpan.textContent = `${account.type === 'expense' ? '-' : '+'} ${formatAmount(account.amount)}`;

            const metaSpan = document.createElement('span');
            metaSpan.className = 'transaction-meta';
            metaSpan.textContent = `${account.date} · ${getCategoryLabel(account.category)}${account.notes ? ' · ' + account.notes : ''}`;

            info.appendChild(amountSpan);
            info.appendChild(metaSpan);

            const rowActions = document.createElement('div');
            rowActions.className = 'transaction-row-actions';

            const editBtn = document.createElement('button');
            editBtn.innerHTML = '<i class="fas fa-edit"></i>';
            editBtn.title = 'تعديل';
            editBtn.addEventListener('click', () => editTransaction(account.id));

            const deleteBtn = document.createElement('button');
            deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
            deleteBtn.title = 'حذف';
            deleteBtn.addEventListener('click', () => deleteAccount(account.id));

            rowActions.appendChild(editBtn);
            rowActions.appendChild(deleteBtn);

            row.appendChild(info);
            row.appendChild(rowActions);
            detailsDiv.appendChild(row);
        });

        card.appendChild(detailsDiv);

        card.querySelector('.btn-toggle-details').addEventListener('click', function() {
            const isHidden = detailsDiv.style.display === 'none';
            detailsDiv.style.display = isHidden ? 'block' : 'none';
            this.querySelector('i').className = isHidden ? 'fas fa-chevron-up' : 'fas fa-chevron-down';
        });
        card.querySelector('.btn-add-transaction').addEventListener('click', () => addTransaction(name));

        container.appendChild(card);
    });
}

// تحديث الملخص في الأسفل
function updateSummary() {
    const owedEl = document.getElementById('total-owed');
    const ownEl = document.getElementById('total-own');
    if (!owedEl || !ownEl) return;

    let totalOwed = 0; // عليك
    let totalOwn = 0;  // لك

    accounts.forEach(account => {
        if (account.type === 'expense') {
            totalOwed += account.amount;
        } else {
            totalOwn += account.amount;
        }
    });

    owedEl.textContent = `عليك: ${formatAmount(totalOwed)}`;
    ownEl.textContent = `لك: ${formatAmount(totalOwn)}`;
}

// إضافة حساب جديد
function addNewAccount() {
    currentEditId = null;
    document.getElementById('modal-title').textContent = 'إضافة حساب جديد';
    document.getElementById('account-form').reset();
    document.getElementById('account-date').valueAsDate = new Date();
    document.getElementById('account-modal').classList.add('active');
}

// إضافة معاملة لاسم موجود
function addTransaction(name) {
    currentEditId = null;
    document.getElementById('modal-title').textContent = `إضافة معاملة - ${name}`;
    document.getElementById('account-form').reset();
    document.getElementById('account-name').value = name;
    document.getElementById('account-date').valueAsDate = new Date();
    document.getElementById('account-modal').classList.add('active');
}

// تعديل معاملة موجودة
function editTransaction(id) {
    const account = accounts.find(a => a.id === id);
    if (!account) return;

    currentEditId = id;
    document.getElementById('modal-title').textContent = 'تعديل معاملة';
    document.getElementById('account-name').value = account.name;
    document.getElementById('account-amount').value = account.amount;
    document.getElementById('account-type').value = account.type;
    document.getElementById('account-category').value = account.category;
    document.getElementById('account-date').value = account.date;
    document.getElementById('account-notes').value = account.notes || '';
    document.getElementById('account-modal').classList.add('active');
}

// إغلاق النافذة
function closeModal() {
    document.getElementById('account-modal').classList.remove('active');
    currentEditId = null;
}

// تقديم النموذج
const accountForm = document.getElementById('account-form');
if (accountForm) {
    accountForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const account = {
            id: currentEditId || Date.now(),
            name: document.getElementById('account-name').value,
            amount: parseFloat(document.getElementById('account-amount').value),
            type: document.getElementById('account-type').value,
            category: document.getElementById('account-category').value,
            date: document.getElementById('account-date').value,
            notes: document.getElementById('account-notes').value,
            createdAt: currentEditId ? accounts.find(a => a.id === currentEditId).createdAt : new Date().toISOString()
        };

        if (currentEditId) {
            const index = accounts.findIndex(a => a.id === currentEditId);
            accounts[index] = account;
        } else {
            accounts.push(account);
        }

        saveData();
        renderAccounts();
        updateSummary();
        closeModal();
    });
}

// التبديل بين القائمة الجانبية
function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('active');
}

// التنقل بين الصفحات
function navigateTo(url) {
    window.location.href = url;
}

// إضافة مبلغ
function addAmount() {
    addNewAccount();
    toggleSidebar();
}

// النسخ الإحتياطي
function backupData() {
    const dataStr = JSON.stringify(accounts, null, 2);
    const dataBlob = new Blob([dataStr], {type: 'application/json'});
    const url = URL.createObjectURL(dataBlob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `backup_${new Date().toISOString().split('T')[0]}.json`;
    link.click();
    toggleSidebar();
}

// استرجاع البيانات
function restoreData() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    input.onchange = function(e) {
        const file = e.target.files[0];
        const reader = new FileReader();
        reader.onload = function(event) {
            try {
                const data = JSON.parse(event.target.result);
                if (Array.isArray(data)) {
                    accounts = data;
                    saveData();
                    renderAccounts();
                    updateSummary();
                    alert('تم استرجاع البيانات بنجاح');
                } else {
                    alert('ملف غير صالح');
                }
            } catch (error) {
                alert('حدث خطأ في قراءة الملف');
            }
        };
        reader.readAsText(file);
    };
    input.click();
    toggleSidebar();
}

// المزامنة مع Google Drive
function syncWithGoogleDrive() {
    alert('خاصية المزامنة مع Google Drive تتطلب إعداد API');
    toggleSidebar();
}

// التكرار التلقائي
function showAutoRepeat() {
    alert('خاصية التكرار التلقائي ستتاح قريباً');
    toggleSidebar();
}

// البحث
function showSearch() {
    const bar = document.getElementById('search-bar');
    const input = document.getElementById('search-input');
    if (!bar || !input) return;

    const isHidden = bar.style.display === 'none' || !bar.style.display;
    if (isHidden) {
        bar.style.display = 'flex';
        input.focus();
    } else {
        closeSearch();
    }
}

// إغلاق شريط البحث
function closeSearch() {
    const bar = document.getElementById('search-bar');
    const input = document.getElementById('search-input');
    if (!bar || !input) return;

    bar.style.display = 'none';
    input.value = '';
    currentSearchTerm = '';
    renderAccounts();
}

// الإشعارات
function showNotifications() {
    alert('لا توجد إشعارات جديدة');
}

// الإعدادات
function showSettings() {
    navigateTo('settings/settings.html');
}

// التواصل والدعم
function showContactSupport() {
    alert('للتواصل والدعم:\nالبريد الإلكتروني: support@accounting.app\nالهاتف: 123456789');
    toggleSidebar();
}

// حول البرنامج
function showAbout() {
    alert('المحاسب - تطبيق الحسابات اليومية\nالإصدار 1.0.0\nجميع الحقوق محفوظة © 2024');
    toggleSidebar();
}

// مشاركة التطبيق
function shareApp() {
    if (navigator.share) {
        navigator.share({
            title: 'المحاسب - تطبيق الحسابات اليومية',
            text: 'تطبيق ممتاز لإدارة حساباتك اليومية',
            url: window.location.href
        });
    } else {
        alert('تم نسخ الرابط: ' + window.location.href);
    }
    toggleSidebar();
}

// تسجيل الخروج
function logout() {
    if (confirm('هل تريد الخروج من التطبيق؟')) {
        // يمكن إضافة منطق تسجيل الخروج هنا
        alert('تم الخروج بنجاح');
    }
    toggleSidebar();
}

// تهريب قيمة لاستخدامها في CSV
function escapeCsvValue(value) {
    const str = String(value ?? '');
    if (/[",\n]/.test(str)) {
        return `"${str.replace(/"/g, '""')}"`;
    }
    return str;
}

// تصدير البيانات كـ CSV
function exportToCSV() {
    let csv = 'الاسم,المبلغ,النوع,التصنيف,التاريخ,الملاحظات\n';
    accounts.forEach(account => {
        csv += [
            escapeCsvValue(account.name),
            escapeCsvValue(account.amount),
            escapeCsvValue(account.type === 'income' ? 'دخل' : 'مصروف'),
            escapeCsvValue(getCategoryLabel(account.category)),
            escapeCsvValue(account.date),
            escapeCsvValue(account.notes)
        ].join(',') + '\n';
    });
    
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `accounts_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
}

// طباعة التقرير
function printReport() {
    window.print();
}

// حذف حساب
function deleteAccount(id) {
    if (confirm('هل أنت متأكد من حذف هذا الحساب؟')) {
        accounts = accounts.filter(a => a.id !== id);
        saveData();
        renderAccounts();
        updateSummary();
    }
}

// تصفية الحسابات حسب النوع (الكل / مصروف / دخل)
function filterAccounts(type) {
    currentFilterType = type;
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.filter === type);
    });
    renderAccounts();
}

// ترتيب الحسابات (الأحدث / الاسم / المبلغ)
function sortAccounts(criteria) {
    currentSortBy = criteria;
    renderAccounts();
}

// الحصول على الاسم المعروض لتصنيف
function getCategoryLabel(value) {
    return CATEGORY_LABELS[value] || value;
}

// التصنيفات المخصصة المضافة من الإعدادات
function getCustomCategories() {
    try {
        const stored = JSON.parse(localStorage.getItem('customCategories') || '[]');
        return Array.isArray(stored) ? stored : [];
    } catch (e) {
        return [];
    }
}

// إضافة التصنيفات المخصصة إلى القائمة المنسدلة
function loadCategories() {
    const select = document.getElementById('account-category');
    if (!select) return;

    const existingValues = Array.from(select.options).map(o => o.value);
    getCustomCategories().forEach(category => {
        if (!existingValues.includes(category)) {
            const option = document.createElement('option');
            option.value = category;
            option.textContent = category;
            select.appendChild(option);
        }
    });
}

// تهريب نص قبل إدراجه كـ HTML لتجنب XSS
function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str ?? '';
    return div.innerHTML;
}

// رمز العملة المختارة في الإعدادات
function getCurrencySymbol() {
    const currency = localStorage.getItem('currency') || 'ريال سعودي';
    return CURRENCY_SYMBOLS[currency] || '';
}

// تنسيق مبلغ مع رمز العملة
function formatAmount(amount) {
    const symbol = getCurrencySymbol();
    const formatted = Number(amount).toLocaleString();
    return symbol ? `${formatted} ${symbol}` : formatted;
}
