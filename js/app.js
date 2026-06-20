// المتغيرات العامة
let accounts = [];
let currentEditId = null;

// تحميل البيانات عند بدء التطبيق
document.addEventListener('DOMContentLoaded', function() {
    loadData();
    renderAccounts();
    updateSummary();
    
    // تعيين تاريخ اليوم كقيمة افتراضية
    document.getElementById('account-date').valueAsDate = new Date();
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

// عرض الحسابات
function renderAccounts() {
    const container = document.getElementById('accounts-list');
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
    
    // تجميع الحسابات حسب الاسم
    const groupedAccounts = {};
    accounts.forEach(account => {
        if (!groupedAccounts[account.name]) {
            groupedAccounts[account.name] = [];
        }
        groupedAccounts[account.name].push(account);
    });
    
    Object.keys(groupedAccounts).forEach(name => {
        const accountGroup = groupedAccounts[name];
        const totalAmount = accountGroup.reduce((sum, acc) => {
            return acc.type === 'expense' ? sum - acc.amount : sum + acc.amount;
        }, 0);
        
        const count = accountGroup.length;
        const latestAccount = accountGroup[accountGroup.length - 1];
        
        const card = document.createElement('div');
        card.className = 'account-card';
        
        const iconClass = totalAmount >= 0 ? 'income' : 'expense';
        const arrowIcon = totalAmount >= 0 ? '▲' : '▼';
        const amountClass = totalAmount >= 0 ? 'positive' : 'negative';
        const amountText = totalAmount >= 0 ? totalAmount.toLocaleString() : Math.abs(totalAmount).toLocaleString();
        
        card.innerHTML = `
            <div class="account-info">
                <div class="account-icon ${iconClass}">
                    <i class="fas fa-user"></i>
                </div>
                <div class="account-details">
                    <h3>${name}</h3>
                    <div>
                        <span class="amount ${amountClass}">${arrowIcon} ${amountText}</span>
                        <span class="count">${count}</span>
                    </div>
                </div>
            </div>
            <div class="account-actions">
                <button onclick="addTransaction('${name}')">
                    <i class="fas fa-plus"></i>
                </button>
            </div>
        `;
        
        container.appendChild(card);
    });
}

// تحديث الملخص في الأسفل
function updateSummary() {
    let totalOwed = 0; // عليك
    let totalOwn = 0;  // لك
    
    accounts.forEach(account => {
        if (account.type === 'expense') {
            totalOwed += account.amount;
        } else {
            totalOwn += account.amount;
        }
    });
    
    document.getElementById('total-owed').textContent = `عليك: ${totalOwed.toLocaleString()}`;
    document.getElementById('total-own').textContent = `لك: ${totalOwn.toLocaleString()}`;
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

// إغلاق النافذة
function closeModal() {
    document.getElementById('account-modal').classList.remove('active');
    currentEditId = null;
}

// تقديم النموذج
document.getElementById('account-form').addEventListener('submit', function(e) {
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
    const searchTerm = prompt('أدخل مصطلح البحث:');
    if (searchTerm) {
        const filtered = accounts.filter(a => 
            a.name.includes(searchTerm) || 
            a.notes.includes(searchTerm)
        );
        alert(`تم العثور على ${filtered.length} نتيجة`);
    }
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

// تصدير البيانات كـ CSV
function exportToCSV() {
    let csv = 'الاسم,المبلغ,النوع,التصنيف,التاريخ,الملاحظات\n';
    accounts.forEach(account => {
        csv += `${account.name},${account.amount},${account.type},${account.category},${account.date},${account.notes}\n`;
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

// تصفية الحسابات
function filterAccounts(type) {
    // يمكن إضافة وظيفة التصفية هنا
    renderAccounts();
}

// ترتيب الحسابات
function sortAccounts(criteria) {
    switch(criteria) {
        case 'name':
            accounts.sort((a, b) => a.name.localeCompare(b.name));
            break;
        case 'amount':
            accounts.sort((a, b) => b.amount - a.amount);
            break;
        case 'date':
            accounts.sort((a, b) => new Date(b.date) - new Date(a.date));
            break;
    }
    renderAccounts();
}
