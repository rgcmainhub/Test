// ================================================
// LUMEN HOTEL — interactions
// ================================================

document.addEventListener('DOMContentLoaded', () => {

  /* ---------- Mobile nav ---------- */
  const burger = document.getElementById('navBurger');
  const navLinks = document.getElementById('navLinks');

  if (burger && navLinks) {
    burger.addEventListener('click', () => {
      const isOpen = navLinks.classList.toggle('is-open');
      burger.setAttribute('aria-expanded', String(isOpen));
      navLinks.style.display = isOpen ? 'flex' : '';
    });

    navLinks.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', () => {
        navLinks.classList.remove('is-open');
        navLinks.style.display = '';
        burger.setAttribute('aria-expanded', 'false');
      });
    });
  }

  /* ---------- Modals (sign in / sign up) ---------- */
  const modals = document.querySelectorAll('.modal');

  function openModal(name) {
    const modal = document.getElementById(`modal-${name}`);
    if (!modal) return;
    modals.forEach(m => m.classList.remove('is-open'));
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
    const firstInput = modal.querySelector('input');
    if (firstInput) firstInput.focus();
  }

  function closeAllModals() {
    modals.forEach(m => {
      m.classList.remove('is-open');
      m.setAttribute('aria-hidden', 'true');
    });
    document.body.style.overflow = '';
  }

  document.querySelectorAll('[data-open-modal]').forEach(btn => {
    btn.addEventListener('click', () => openModal(btn.dataset.openModal));
  });

  document.querySelectorAll('[data-close-modal]').forEach(el => {
    el.addEventListener('click', closeAllModals);
  });

  document.querySelectorAll('[data-switch-modal]').forEach(btn => {
    btn.addEventListener('click', () => openModal(btn.dataset.switchModal));
  });

  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') closeAllModals();
  });

  /* ---------- Sign in / Sign up form handling (demo only, no backend) ---------- */
  const signinForm = document.getElementById('signinForm');
  const signupForm = document.getElementById('signupForm');

  if (signinForm) {
    signinForm.addEventListener('submit', e => {
      e.preventDefault();
      closeAllModals();
      signinForm.reset();
    });
  }

  if (signupForm) {
    signupForm.addEventListener('submit', e => {
      e.preventDefault();
      closeAllModals();
      signupForm.reset();
    });
  }

  /* ---------- Booking widget ---------- */
  const bookingForm = document.getElementById('bookingForm');
  const bookingNote = document.getElementById('bookingNote');

  if (bookingForm) {
    bookingForm.addEventListener('submit', e => {
      e.preventDefault();
      const inDate = document.getElementById('checkin').value;
      const outDate = document.getElementById('checkout').value;

      if (inDate && outDate && outDate <= inDate) {
        bookingNote.textContent = 'Check-out should be after check-in.';
        return;
      }
      bookingNote.textContent = 'Rooms available for those dates — pick one below.';
      document.getElementById('rooms').scrollIntoView({ behavior: 'smooth' });
    });
  }

  /* ---------- Reservation form ---------- */
  const reservationForm = document.getElementById('reservationForm');
  const resNote = document.getElementById('resNote');

  if (reservationForm) {
    reservationForm.addEventListener('submit', e => {
      e.preventDefault();
      const name = document.getElementById('resName').value.trim();
      resNote.textContent = `Thanks, ${name.split(' ')[0] || 'guest'} — your reservation request is in. We'll email a confirmation shortly.`;
      reservationForm.reset();
    });
  }

  /* ---------- Sensible default dates (today / tomorrow) ---------- */
  const today = new Date();
  const tomorrow = new Date(today);
  tomorrow.setDate(today.getDate() + 1);
  const fmt = d => d.toISOString().split('T')[0];

  ['checkin', 'resIn'].forEach(id => {
    const el = document.getElementById(id);
    if (el) { el.min = fmt(today); }
  });
  ['checkout', 'resOut'].forEach(id => {
    const el = document.getElementById(id);
    if (el) { el.min = fmt(tomorrow); }
  });

  /* ---------- Scroll reveal for room cards ---------- */
  const reveals = document.querySelectorAll('.reveal');
  if ('IntersectionObserver' in window && reveals.length) {
    const io = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          io.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15 });
    reveals.forEach(el => io.observe(el));
  } else {
    reveals.forEach(el => el.classList.add('is-visible'));
  }

});
