document.querySelectorAll(".flash").forEach((flash) => {
  window.setTimeout(() => flash.classList.add("fade"), 3500);
});

document.querySelectorAll(".js-chat-room-form").forEach((form) => {
  const roomType = form.querySelector(".js-chat-room-type");
  const arenaField = form.querySelector(".js-chat-arena-field");
  const challengeField = form.querySelector(".js-chat-challenge-field");
  const arenaSelect = form.querySelector(".js-chat-arena-select");
  const challengeSelect = form.querySelector(".js-chat-challenge-select");

  const filterChallenges = () => {
    if (!arenaSelect || !challengeSelect) {
      return;
    }
    const arenaId = arenaSelect.value;
    let firstVisible = null;
    challengeSelect.querySelectorAll("option").forEach((option) => {
      const visible = option.dataset.arenaId === arenaId;
      option.hidden = !visible;
      option.disabled = !visible;
      if (visible && !firstVisible) {
        firstVisible = option;
      }
    });
    if (challengeSelect.selectedOptions[0]?.disabled && firstVisible) {
      challengeSelect.value = firstVisible.value;
    }
  };

  const syncRoomFields = () => {
    const value = roomType?.value;
    if (arenaField) {
      arenaField.hidden = value === "GLOBAL";
    }
    if (challengeField) {
      challengeField.hidden = value !== "CHALLENGE";
    }
    filterChallenges();
  };

  roomType?.addEventListener("change", syncRoomFields);
  arenaSelect?.addEventListener("change", filterChallenges);
  syncRoomFields();
});

document.querySelectorAll(".js-admin-arena-select").forEach((arenaSelect) => {
  const form = arenaSelect.closest("form");
  const templateSelect = form?.querySelector(".js-admin-template-select");
  const preview = form?.querySelector(".js-admin-template-preview");

  const syncTemplates = () => {
    if (!templateSelect) {
      return;
    }
    const arenaId = arenaSelect.value;
    let firstVisible = null;
    templateSelect.querySelectorAll("option").forEach((option) => {
      const visible = option.dataset.arenaId === arenaId;
      option.hidden = !visible;
      option.disabled = !visible;
      if (visible && !firstVisible) {
        firstVisible = option;
      }
    });
    if (templateSelect.selectedOptions[0]?.disabled && firstVisible) {
      templateSelect.value = firstVisible.value;
    }
    if (preview) {
      preview.textContent = templateSelect.selectedOptions[0]?.dataset.templateBody || "";
    }
  };

  arenaSelect.addEventListener("change", syncTemplates);
  templateSelect?.addEventListener("change", syncTemplates);
  syncTemplates();
});

document.querySelectorAll(".js-structured-answer").forEach((shell) => {
  const form = shell.closest("form");
  const output = shell.querySelector(".js-structured-content");
  const parts = [...shell.querySelectorAll(".js-answer-part")];

  form?.addEventListener("submit", () => {
    if (!output) {
      return;
    }
    output.value = parts
      .filter((field) => field.value.trim())
      .map((field) => `${field.dataset.label}\n${field.value.trim()}`)
      .join("\n\n");
  });
});

document.querySelectorAll(".js-template-select").forEach((select) => {
  const form = select.closest("form") || document;
  const preview = form.querySelector(".js-template-preview");
  const paint = () => {
    if (!preview || !select.selectedOptions.length) {
      return;
    }
    const option = select.selectedOptions[0];
    preview.className = `template-preview-card js-template-preview preview-${option.dataset.class || "coding"}`;
    preview.innerHTML = `
      <span class="template-icon">${option.dataset.icon || "template"}</span>
      <div>
        <strong>${option.textContent.split(" - ")[0]}</strong>
        <p>${option.dataset.description || ""}</p>
        <small>${option.dataset.body || ""}</small>
      </div>
    `;
  };
  select.addEventListener("change", paint);
  paint();
});

document.querySelectorAll(".js-template-answer").forEach((form) => {
  const output = form.querySelector(".js-template-content");
  form.addEventListener("submit", (event) => {
    if (!output) {
      return;
    }
    const payload = {};
    const grouped = {};
    form.querySelectorAll(".js-template-field").forEach((field) => {
      const key = field.dataset.field;
      if (!key) {
        return;
      }
      if (field.type === "checkbox") {
        grouped[key] = grouped[key] || [];
        if (field.checked) {
          grouped[key].push(field.value);
        }
        return;
      }
      if (field.type === "radio") {
        if (field.checked) {
          payload[key] = field.value;
        }
        return;
      }
      payload[key] = field.value.trim();
    });
    Object.assign(payload, grouped);
    output.value = JSON.stringify(payload);
  });
});

document.querySelectorAll(".js-code-editor").forEach((editor) => {
  const preview = editor.closest(".structured-answer")?.querySelector(".js-code-preview");
  const highlight = (source) => {
    const escaped = source
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;");
    return escaped
      .replace(/\b(public|private|class|static|void|int|long|String|return|if|else|for|while|new|boolean|true|false|null)\b/g, '<span class="code-keyword">$1</span>')
      .replace(/(".*?")/g, '<span class="code-string">$1</span>')
      .replace(/(\/\/.*)/g, '<span class="code-comment">$1</span>');
  };
  const paint = () => {
    if (!preview) {
      return;
    }
    preview.innerHTML = highlight(editor.value || "// code preview");
  };
  editor.addEventListener("input", paint);
  paint();
});

document.querySelectorAll(".js-chat-popover-toggle").forEach((button) => {
  const popover = document.querySelector(".js-chat-popover");
  button.addEventListener("click", () => {
    if (popover) {
      popover.hidden = !popover.hidden;
    }
  });
});

document.querySelectorAll(".js-chat-stream[data-chat-auto-refresh='true']").forEach((stream) => {
  stream.scrollTop = stream.scrollHeight;
  const composer = document.querySelector(".chat-composer");
  const textarea = composer?.querySelector("textarea");
  const roomParams = new URLSearchParams();
  const roomType = stream.dataset.roomType;
  const arenaId = stream.dataset.arenaId;
  const challengeId = stream.dataset.challengeId;
  if (roomType) {
    roomParams.set("roomType", roomType);
  }
  if (arenaId) {
    roomParams.set("arenaId", arenaId);
  }
  if (challengeId) {
    roomParams.set("challengeId", challengeId);
  }

  const appendMessage = (message) => {
    stream.querySelector(".empty-state")?.remove();

    const article = document.createElement("article");
    article.className = "chat-message";

    const header = document.createElement("div");
    const chip = document.createElement("span");
    chip.className = "profile-chip";
    const avatar = document.createElement("img");
    avatar.src = message.authorAvatarUrl;
    avatar.alt = "";
    const author = document.createElement("strong");
    author.textContent = message.authorName;
    const time = document.createElement("span");
    time.textContent = message.displayTime;

    chip.append(avatar, author);
    header.append(chip, time);

    const content = document.createElement("p");
    content.textContent = message.content;
    article.append(header, content);
    stream.append(article);
    stream.scrollTop = stream.scrollHeight;
  };

  const connectChatSocket = () => {
    if (!window.WebSocket || !roomType) {
      return null;
    }
    const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
    const socket = new WebSocket(`${protocol}//${window.location.host}/ws/chat?${roomParams.toString()}`);

    socket.addEventListener("message", (event) => {
      const payload = JSON.parse(event.data);
      if (payload.type === "message") {
        appendMessage(payload.message);
      }
    });

    socket.addEventListener("close", () => {
      stream.dataset.chatSocketOpen = "false";
    });
    socket.addEventListener("open", () => {
      stream.dataset.chatSocketOpen = "true";
    });
    return socket;
  };

  const socket = connectChatSocket();

  composer?.addEventListener("submit", (event) => {
    if (!socket || socket.readyState !== WebSocket.OPEN || !textarea?.value.trim()) {
      return;
    }
    event.preventDefault();
    socket.send(JSON.stringify({ content: textarea.value.trim() }));
    textarea.value = "";
    textarea.focus();
  });

  const refresh = async () => {
    if (stream.dataset.chatSocketOpen === "true") {
      return;
    }
    if (document.activeElement === textarea && textarea.value.trim()) {
      return;
    }
    try {
      const response = await fetch(window.location.href, { headers: { "X-Requested-With": "fetch" } });
      const html = await response.text();
      const doc = new DOMParser().parseFromString(html, "text/html");
      const freshStream = doc.querySelector(".js-chat-stream");
      if (freshStream && freshStream.innerHTML !== stream.innerHTML) {
        stream.innerHTML = freshStream.innerHTML;
        stream.scrollTop = stream.scrollHeight;
      }
    } catch (error) {
      // Keep the current room usable if a refresh request fails.
    }
  };
  window.setInterval(refresh, 5000);
});

document.querySelectorAll(".js-leaderboard-list").forEach((list) => {
  const panel = list.closest("[data-leaderboard-scope]");
  const scope = panel?.dataset.leaderboardScope;
  if (!panel || !scope) {
    return;
  }

  const params = new URLSearchParams({ scope });
  if (panel.dataset.leaderboardArenaId) {
    params.set("arenaId", panel.dataset.leaderboardArenaId);
  }
  if (panel.dataset.leaderboardChallengeId) {
    params.set("challengeId", panel.dataset.leaderboardChallengeId);
  }

  const render = (leaders) => {
    list.innerHTML = "";
    if (!leaders.length) {
      const empty = document.createElement("li");
      empty.innerHTML = `
        <span class="rank">-</span>
        <span>No ranked players yet</span>
        <strong class="score-pill">0 XP</strong>
      `;
      list.append(empty);
      return;
    }
    leaders.forEach((leader, index) => {
      const item = document.createElement("li");
      const rank = document.createElement("span");
      rank.className = "rank";
      rank.textContent = String(index + 1);

      const identity = document.createElement("span");
      const chip = document.createElement("span");
      chip.className = "profile-chip";
      const avatar = document.createElement("img");
      avatar.src = leader.avatarUrl;
      avatar.alt = "";
      const name = document.createElement("strong");
      name.textContent = leader.name;
      const rankLabel = document.createElement("small");
      rankLabel.textContent = leader.rank;
      chip.append(avatar, name);
      identity.append(chip, rankLabel);

      const score = document.createElement("strong");
      score.className = "score-pill";
      score.textContent = `${leader.xp} XP`;

      item.append(rank, identity, score);
      list.append(item);
    });
  };

  const refreshLeaderboard = async () => {
    try {
      const response = await fetch(`/api/leaderboards?${params.toString()}`, {
        headers: { Accept: "application/json" }
      });
      if (response.ok) {
        render(await response.json());
      }
    } catch (error) {
      // Server-rendered standings remain visible if the API refresh fails.
    }
  };

  refreshLeaderboard();
  window.setInterval(refreshLeaderboard, 10000);
});

document.querySelectorAll(".chat-composer textarea").forEach((textarea) => {
  textarea.addEventListener("keydown", (event) => {
    if (event.ctrlKey && event.key === "Enter") {
      event.preventDefault();
      textarea.closest("form")?.requestSubmit();
    }
  });
});

document.querySelectorAll(".js-creator-tools-toggle").forEach((toggle) => {
  const body = toggle.closest(".panel")?.querySelector(".js-creator-tools-body");
  const sync = () => {
    if (body) {
      body.hidden = !toggle.checked;
    }
  };
  toggle.addEventListener("change", sync);
  sync();
});
