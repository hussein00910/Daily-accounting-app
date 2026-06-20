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

// إخفاء المحتوى فوراً إذا كان قفل التطبيق مفعلاً، لتفادي ظهوره للحظة قبل طلب كلمة المرور
if (isAppLockEnabled() && sessionStorage.getItem('appUnlocked') !== 'true') {
    document.documentElement.style.visibility = 'hidden';
}

// تحميل البيانات عند بدء التطبيق
document.addEventListener('DOMContentLoaded', function() {
    initAppLock();
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

// مفاتيح localStorage التي تمثل قاعدة بيانات التطبيق الكاملة
const BACKUP_KEYS = ['accountingData', 'appSettings', 'customCategories', 'currency', 'userName', 'appLockPin'];

// بناء ملف نسخة احتياطية كامل (بصيغة قاعدة بيانات منظمة، لا مصفوفة حسابات فقط)
function buildBackupPayload() {
    const data = {};
    BACKUP_KEYS.forEach(key => {
        const value = localStorage.getItem(key);
        if (value !== null) data[key] = value;
    });
    return {
        app: 'daily-accounting-app',
        version: 1,
        exportedAt: new Date().toISOString(),
        data
    };
}

// تطبيق نسخة احتياطية محفوظة على قاعدة البيانات المحلية
function applyBackupPayload(parsed) {
    if (Array.isArray(parsed)) {
        // نسخة قديمة: مصفوفة حسابات فقط
        localStorage.setItem('accountingData', JSON.stringify(parsed));
        return;
    }
    if (!parsed || typeof parsed !== 'object' || !parsed.data || typeof parsed.data !== 'object') {
        throw new Error('invalid backup file');
    }
    Object.keys(parsed.data).forEach(key => {
        if (BACKUP_KEYS.includes(key)) {
            localStorage.setItem(key, parsed.data[key]);
        }
    });
}

// النسخ الإحتياطي (حفظ محلي، أو مشاركة/رفع للسحابة عبر قائمة المشاركة في الجهاز)
async function backupData() {
    const json = JSON.stringify(buildBackupPayload(), null, 2);
    const fileName = `accounting_backup_${new Date().toISOString().split('T')[0]}.json`;

    if (window.Capacitor && window.Capacitor.isNativePlatform()) {
        try {
            const { uri } = await window.Capacitor.Plugins.Filesystem.writeFile({
                path: fileName,
                data: json,
                directory: 'CACHE',
                encoding: 'utf8'
            });
            await window.Capacitor.Plugins.Share.share({
                title: 'نسخة احتياطية - المحاسب',
                text: 'نسخة احتياطية من بيانات تطبيق المحاسب',
                url: uri,
                dialogTitle: 'حفظ النسخة الاحتياطية أو مشاركتها على السحابة'
            });
        } catch (err) {
            alert('حدث خطأ أثناء إنشاء النسخة الاحتياطية: ' + (err && err.message ? err.message : err));
        }
    } else {
        const blob = new Blob([json], { type: 'application/json' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = fileName;
        link.click();
    }
    toggleSidebar();
}

// استرجاع البيانات من نسخة احتياطية
function restoreData() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json,application/json';
    input.onchange = function(e) {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = function(event) {
            try {
                const parsed = JSON.parse(event.target.result);
                applyBackupPayload(parsed);
                alert('تم استرجاع البيانات بنجاح');
                location.reload();
            } catch (error) {
                alert('حدث خطأ في قراءة الملف، أو أن الملف غير صالح');
            }
        };
        reader.readAsText(file);
    };
    input.click();
    toggleSidebar();
}

// رفع نسخة احتياطية للسحابة (يفتح قائمة مشاركة الجهاز لاختيار جوجل درايف أو أي تطبيق آخر)
function syncWithGoogleDrive() {
    backupData();
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
    const modal = document.getElementById('contact-modal');
    if (modal) {
        modal.classList.add('active');
    }
    toggleSidebar();
}

// إغلاق نافذة التواصل والدعم
function closeContactModal() {
    const modal = document.getElementById('contact-modal');
    if (modal) {
        modal.classList.remove('active');
    }
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
            escapeCsvValue(account.type === 'income' ? 'له' : 'عليه'),
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

// ===== قفل التطبيق =====

// تجزئة كلمة المرور بحيث لا تُخزن كنص صريح في localStorage
async function hashPin(pin) {
    const data = new TextEncoder().encode(pin);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    return Array.from(new Uint8Array(hashBuffer)).map(b => b.toString(16).padStart(2, '0')).join('');
}

// هل قفل التطبيق مفعّل وله كلمة مرور محفوظة؟
function isAppLockEnabled() {
    try {
        const settings = JSON.parse(localStorage.getItem('appSettings') || '{}');
        return !!settings['app-lock'] && !!localStorage.getItem('appLockPin');
    } catch (e) {
        return false;
    }
}

// تهيئة قفل التطبيق عند بدء كل صفحة
function initAppLock() {
    if (!isAppLockEnabled() || sessionStorage.getItem('appUnlocked') === 'true') {
        document.documentElement.style.visibility = 'visible';
        return;
    }
    showLockScreen();
}

// عرض شاشة قفل التطبيق وانتظار كلمة المرور الصحيحة
function showLockScreen() {
    const overlay = document.createElement('div');
    overlay.className = 'app-lock-overlay';
    overlay.innerHTML = `
        <div class="app-lock-box">
            <i class="fas fa-lock"></i>
            <h2>التطبيق مقفل</h2>
            <p>أدخل كلمة المرور للمتابعة</p>
            <input type="password" id="app-lock-input" inputmode="numeric" autocomplete="off">
            <div class="app-lock-error" id="app-lock-error"></div>
            <button type="button" id="app-lock-submit" class="btn-save">فتح</button>
        </div>
    `;
    document.body.appendChild(overlay);
    document.documentElement.style.visibility = 'visible';

    const input = overlay.querySelector('#app-lock-input');
    const errorEl = overlay.querySelector('#app-lock-error');
    input.focus();

    async function tryUnlock() {
        const hash = await hashPin(input.value);
        if (hash === localStorage.getItem('appLockPin')) {
            sessionStorage.setItem('appUnlocked', 'true');
            overlay.remove();
        } else {
            errorEl.textContent = 'كلمة مرور غير صحيحة';
            input.value = '';
            input.focus();
        }
    }

    overlay.querySelector('#app-lock-submit').addEventListener('click', tryUnlock);
    input.addEventListener('keydown', e => {
        if (e.key === 'Enter') tryUnlock();
    });
}

// تعيين كلمة مرور جديدة لقفل التطبيق (تُستخدم عند التفعيل من الإعدادات)
async function setAppLockPin() {
    const pin = prompt('أدخل كلمة مرور لقفل التطبيق (4 أحرف على الأقل):');
    if (!pin) return false;
    if (pin.length < 4) {
        alert('كلمة المرور يجب أن تكون 4 أحرف على الأقل');
        return false;
    }
    const confirmPin = prompt('أعد إدخال كلمة المرور للتأكيد:');
    if (pin !== confirmPin) {
        alert('كلمتا المرور غير متطابقتين');
        return false;
    }
    localStorage.setItem('appLockPin', await hashPin(pin));
    sessionStorage.setItem('appUnlocked', 'true');
    return true;
}
