async function apiGet(url) {
  const res = await fetch(url);
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `GET ${url} failed`);
  }
  return res.json();
}

async function apiPost(url, body) {
  const res = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });

  if (!res.ok) {
    let msg = "";
    try {
      msg = await res.text();
    } catch (_) {}
    throw new Error(msg || `POST ${url} failed`);
  }
  return res.json();
}