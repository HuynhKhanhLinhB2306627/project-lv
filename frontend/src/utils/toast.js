import Swal from 'sweetalert2';


const Toast = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.addEventListener('mouseenter', Swal.stopTimer);
    toast.addEventListener('mouseleave', Swal.resumeTimer);
  }
});

export const toast = {
  success: (message) => {
    Toast.fire({
      icon: 'success',
      title: message
    });
  },
  error: (message) => {
    Toast.fire({
      icon: 'error',
      title: message || 'Có lỗi xảy ra!'
    });
  },
  warning: (message) => {
    Toast.fire({
      icon: 'warning',
      title: message
    });
  },
  info: (message) => {
    Toast.fire({
      icon: 'info',
      title: message
    });
  },
  
  confirm: async (title, text, icon = 'warning') => {
    const result = await Swal.fire({
      title: title,
      text: text,
      icon: icon,
      showCancelButton: true,
      confirmButtonColor: '#0d6efd',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Hủy',
      reverseButtons: true
    });
    return result.isConfirmed;
  },
  
  alert: (title, text, icon = 'info') => {
    Swal.fire({
      title: title,
      text: text,
      icon: icon,
      confirmButtonColor: '#0d6efd'
    });
  }
};
