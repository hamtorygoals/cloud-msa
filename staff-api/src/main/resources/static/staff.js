async function apiGet(url) {
  const res = await fetch(url);
  const text = await res.text();

  if (!res.ok) {
    throw new Error(text || `GET ${url} failed`);
  }

  try {
    return JSON.parse(text);
  } catch (_) {
    // core가 텍스트로 에러를 던지는 경우 대비
    return text;
  }
}

function showError(msg) {
  const box = document.getElementById("errorBox");
  box.textContent = msg;
  box.classList.remove("hidden");
}

function clearError() {
  const box = document.getElementById("errorBox");
  box.textContent = "";
  box.classList.add("hidden");
}

function showResult(data) {
  document.getElementById("resultCard").classList.remove("hidden");
  document.getElementById("rEvent").textContent = data.eventId ?? "-";
  document.getElementById("rSeat").textContent = data.seatId ?? "-";
  document.getElementById("rCode").textContent = data.code4 ?? "-";

  const at = data.reservedAt ? new Date(data.reservedAt) : null;
  document.getElementById("rAt").textContent = at ? at.toLocaleString() : "-";
}

function hideResult() {
  document.getElementById("resultCard").classList.add("hidden");
}

function normalizeCode4(v) {
  // 숫자만 남기고 4자리 제한
  const onlyNum = (v || "").replace(/\D/g, "").slice(0, 4);
  return onlyNum;
}

document.addEventListener("DOMContentLoaded", () => {
  const eventIdEl = document.getElementById("eventId");
  const codeEl = document.getElementById("code4");
  const lookupBtn = document.getElementById("lookupBtn");
  const clearBtn = document.getElementById("clearBtn");

  codeEl.addEventListener("input", () => {
    codeEl.value = normalizeCode4(codeEl.value);
  });

  async function lookup() {
    clearError();
    hideResult();

    const eventId = eventIdEl.value.trim();
    const code4 = normalizeCode4(codeEl.value);

    if (!eventId) return showError("eventId를 입력하세요.");
    if (code4.length !== 4) return showError("인증번호 4자리를 입력하세요.");

    lookupBtn.disabled = true;
    lookupBtn.textContent = "조회중...";

    try {
      // staff-api의 endpoint 호출 (same-origin)
      const data = await apiGet(`/staff/lookup?eventId=${encodeURIComponent(eventId)}&code4=${encodeURIComponent(code4)}`);
      showResult(data);
    } catch (e) {
      showError(e.message || "조회 실패");
    } finally {
      lookupBtn.disabled = false;
      lookupBtn.textContent = "조회";
    }
  }

  lookupBtn.addEventListener("click", lookup);

  // Enter 키로 조회
  codeEl.addEventListener("keydown", (e) => {
    if (e.key === "Enter") lookup();
  });

  clearBtn.addEventListener("click", () => {
    clearError();
    hideResult();
    codeEl.value = "";
    codeEl.focus();
  });
});