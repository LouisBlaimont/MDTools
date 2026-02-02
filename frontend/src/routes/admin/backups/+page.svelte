<script lang="ts">
  import { onMount, tick } from "svelte";
  import { goto } from "$app/navigation";
  import { toast } from "@zerodevx/svelte-toast";
  import { apiFetch } from "$lib/utils/fetch";
  import { isAdmin, isWebmaster } from "$lib/stores/user_stores";
  import Icon from "@iconify/svelte";
  import { browser } from "$app/environment";

  type Backups = { daily: any; weekly: any };

  let backups: Backups | null = null;

  let selected: "daily" | "weekly" = "daily";
  let password = "";
  let confirmText = "";

  let jobId: string | null = null;
  let status: any = null;

  let logs = "";
  let logOffset = 0;

  let loadingBackups = false;
  let startingRestore = false;
  let polling = false;

  let error = "";
  let info = "";

  let logsEl: HTMLPreElement | null = null;
  let autoScroll = true;

  const restoreOptions = [
    {
      key: "daily" as const,
      title: "Daily restore",
      subtitle: "Last snapshot taken at 00:00 (Mon–Sat).",
      badge: "DAILY",
      badgeClasses: "bg-blue-50 text-blue-700 ring-1 ring-inset ring-blue-700/10",
      icon: "material-symbols:calendar-today",
    },
    {
      key: "weekly" as const,
      title: "Weekly restore",
      subtitle: "Last snapshot taken at 00:00 (Sunday).",
      badge: "WEEKLY",
      badgeClasses: "bg-purple-50 text-purple-700 ring-1 ring-inset ring-purple-700/10",
      icon: "material-symbols:calendar-month",
    },
  ];

  const JOB_KEY = "mdtools_restore_job_id";

  function saveJobId(id: string) {
    if (!browser) return;
    localStorage.setItem(JOB_KEY, id);
  }

  function loadJobId(): string | null {
    if (!browser) return null;
    return localStorage.getItem(JOB_KEY);
  }

  function clearJobId() {
    if (!browser) return;
    localStorage.removeItem(JOB_KEY);
  }

  function formatTime(value: any) {
    if (!value?.time) return "none";
    try {
      return new Date(value.time).toLocaleString();
    } catch {
      return value.time;
    }
  }

  async function loadBackups() {
    error = "";
    info = "";
    loadingBackups = true;

    try {
      const res = await apiFetch("/api/admin/backups");
      if (!res.ok) throw new Error(await res.text());
      backups = await res.json();
    } catch (e: any) {
      error = e?.message || "Failed to load backups";
    } finally {
      loadingBackups = false;
    }
  }

  function resetJobUi() {
    jobId = null;
    status = null;
    logs = "";
    logOffset = 0;
    polling = false;
    clearJobId();
  }

  function validateRestore() {
    if (confirmText !== "RESTORE") {
      error = 'Type "RESTORE" to confirm';
      return false;
    }
    if (!password) {
      error = "Restore password required";
      return false;
    }
    return true;
  }

  async function startRestore() {
    error = "";
    info = "";

    if (!validateRestore()) return;

    startingRestore = true;
    resetJobUi();

    try {
      const res = await apiFetch("/api/admin/backups/restore", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ type: selected, password }),
      });

      const txt = await res.text();
      if (res.status !== 202) throw new Error(txt || "Restore failed");

      const parsed = JSON.parse(txt);
      jobId = parsed.jobId;   
      saveJobId(parsed.jobId);
      info = `Restore started (job: ${jobId}). The app may be unavailable during the operation.`;
      toast.push("Restore started.");

      // Start polling logs/status
      pollJob();
    } catch (e: any) {
      error = e?.message || "Restore failed";
      toast.push("Restore failed.");
    } finally {
      startingRestore = false;
    }
  }

  async function pollJob() {
    if (!jobId) return;
    polling = true;

    while (jobId) {
      try {
        // logs endpoint also returns job meta, so we can rely mostly on it
        const lg = await apiFetch(`/api/admin/backups/jobs/${jobId}/logs?offset=${logOffset}`);
        if (lg.ok) {
          const data = await lg.json();
          logs += data.content || "";
          logOffset = data.nextOffset ?? logOffset;
          status = data.job ?? status;

          if (autoScroll && logsEl) {
            await tick();
            logsEl.scrollTop = logsEl.scrollHeight;
          }
        }

        const s = status?.status;
        if (s === "succeeded" || s === "failed") {
          polling = false;
          info = `Job finished: ${s}`;
          toast.push(s === "succeeded" ? "Restore finished." : "Restore failed.");
          clearJobId(); // <--- important
          break;
        }
      } catch {
        // ignore transient errors
      }

      await new Promise((r) => setTimeout(r, 2000));
    }
  }

  function choose(type: "daily" | "weekly") {
    selected = type;
  }

  onMount(async () => {
    if (!$isAdmin && !$isWebmaster) {
      goto("/unauthorized");
      return;
    }
    await loadBackups();
    const saved = loadJobId();
    if (saved) {
      jobId = saved;
      info = `Resuming restore job (job: ${jobId}) after page reload...`;
      pollJob();
    }
  });
</script>

<svelte:head>
  <title>Backups</title>
</svelte:head>

<div class="p-8 space-y-10">
  <section class="bg-white shadow-lg rounded-xl p-6">
    <div class="flex items-start justify-between gap-6">
      <div>
        <h2 class="text-2xl font-semibold">Backup restore</h2>
        <p class="text-gray-600 mt-1 max-w-2xl">
          Restore the full MDTools state (pictures + databases) using the latest daily or weekly snapshot.
        </p>
      </div>

      <button
        class="inline-flex items-center gap-2 bg-gray-100 hover:bg-gray-200 text-gray-800 px-4 py-2 rounded-lg transition"
        onclick={loadBackups}
        disabled={loadingBackups || polling || startingRestore}
        aria-label="Refresh"
      >
        <Icon icon="material-symbols:refresh" width="20" height="20" />
        {loadingBackups ? "Refreshing..." : "Refresh"}
      </button>
    </div>

    {#if error}
      <div class="mt-6 bg-red-50 border border-red-200 text-red-800 rounded-lg p-4">
        <div class="flex items-start gap-3">
          <Icon icon="material-symbols:error-circle-rounded" width="22" height="22" />
          <div class="text-sm whitespace-pre-wrap">{error}</div>
        </div>
      </div>
    {/if}

    {#if info}
      <div class="mt-6 bg-green-50 border border-green-200 text-green-800 rounded-lg p-4">
        <div class="flex items-start gap-3">
          <Icon icon="material-symbols:check-circle-rounded" width="22" height="22" />
          <div class="text-sm whitespace-pre-wrap">{info}</div>
        </div>
      </div>
    {/if}

    <!-- Snapshot summary -->
    <div class="mt-8 grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-gray-50 border border-gray-200 rounded-xl p-6">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-medium">Latest daily</h3>
          <span class="rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset bg-blue-50 text-blue-700 ring-blue-700/10">
            DAILY
          </span>
        </div>
        <p class="text-gray-700 mt-2">
          <span class="text-gray-500">Time:</span>
          <span class="font-medium">{backups ? formatTime(backups.daily) : "…"}</span>
        </p>
      </div>

      <div class="bg-gray-50 border border-gray-200 rounded-xl p-6">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-medium">Latest weekly</h3>
          <span class="rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset bg-purple-50 text-purple-700 ring-purple-700/10">
            WEEKLY
          </span>
        </div>
        <p class="text-gray-700 mt-2">
          <span class="text-gray-500">Time:</span>
          <span class="font-medium">{backups ? formatTime(backups.weekly) : "…"}</span>
        </p>
      </div>
    </div>

    <!-- Restore selection -->
    <div class="mt-10 bg-gray-50 border border-gray-200 rounded-xl p-6">
      <div class="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h3 class="text-lg font-medium">Choose restore target</h3>
          <p class="text-gray-600 text-sm mt-1">
            Only the latest daily and weekly snapshots exist (2 total). Restoring will stop the app temporarily.
          </p>
        </div>

        <div class="flex items-center gap-3">
          <span
            class="rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset bg-yellow-50 text-yellow-800 ring-yellow-800/10"
          >
            HIGH IMPACT
          </span>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mt-6">
        {#each restoreOptions as opt}
          <button
            class={`text-left border rounded-xl p-5 transition shadow-sm hover:shadow-md ${
              selected === opt.key
                ? "border-teal-500 ring-2 ring-teal-200 bg-white"
                : "border-gray-200 bg-white hover:border-gray-300"
            }`}
            onclick={() => choose(opt.key)}
            disabled={polling || startingRestore}
          >
            <div class="flex items-start justify-between gap-4">
              <div class="flex items-start gap-3">
                <div class="mt-1 text-gray-700">
                  <Icon icon={opt.icon} width="24" height="24" />
                </div>
                <div>
                  <div class="font-semibold text-gray-900">{opt.title}</div>
                  <div class="text-sm text-gray-600 mt-1">{opt.subtitle}</div>
                </div>
              </div>

              <span class={`rounded-md px-2 py-1 text-xs font-medium ${opt.badgeClasses}`}>
                {opt.badge}
              </span>
            </div>
          </button>
        {/each}
      </div>

      <!-- Confirmation + password -->
      <div class="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div class="lg:col-span-1">
          <!-- svelte-ignore a11y_label_has_associated_control -->
          <label class="block text-sm font-medium text-gray-700 mb-2">Restore password</label>
          <input
            type="password"
            bind:value={password}
            placeholder="Enter restore password"
            class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
            disabled={polling || startingRestore}
          />
        </div>

        <div class="lg:col-span-1">
          <!-- svelte-ignore a11y_label_has_associated_control -->
          <label class="block text-sm font-medium text-gray-700 mb-2">Confirmation</label>
          <input
            type="text"
            bind:value={confirmText}
            placeholder='Type "RESTORE"'
            class="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-teal-500"
            disabled={polling || startingRestore}
          />
          <p class="text-xs text-gray-500 mt-2">
            This will overwrite current state (pictures + databases).
          </p>
        </div>

        <div class="lg:col-span-1 flex items-end">
          <button
            class={`w-full inline-flex items-center justify-center gap-2 px-6 py-3 rounded-lg text-white transform transition ${
              polling || startingRestore
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-gradient-to-r from-teal-500 to-teal-700 hover:scale-105"
            }`}
            onclick={startRestore}
            disabled={polling || startingRestore}
          >
            <Icon icon="material-symbols:restart-alt-rounded" width="22" height="22" />
            {startingRestore ? "Starting..." : "Restore now"}
          </button>
        </div>
      </div>

      <!-- Warning box -->
      <div class="mt-6 bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <div class="flex items-start gap-3">
          <Icon icon="material-symbols:warning-rounded" width="22" height="22" />
          <div class="text-sm text-yellow-900">
            <div class="font-semibold">Warning</div>
            <ul class="list-disc pl-5 mt-2 space-y-1">
              <li>Restoring can take time depending on pictures volume size.</li>
              <li>MDTools will be temporarily unavailable during restore.</li>
              <li>Only admins should perform this operation.</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Job status + logs -->
    <div class="mt-10 bg-gray-50 border border-gray-200 rounded-xl p-6">
      <div class="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h3 class="text-lg font-medium">Restore job</h3>
          <p class="text-sm text-gray-600 mt-1">
            Live logs from the backup service. You can disable auto-scroll if you want to inspect older logs.
          </p>
        </div>

        <div class="flex items-center gap-4">
          <label class="inline-flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" bind:checked={autoScroll} />
            Auto-scroll
          </label>

          <span class="rounded-md bg-gray-100 px-2 py-1 text-xs font-medium text-gray-800 ring-1 ring-inset ring-gray-800/10">
            {polling ? "RUNNING" : "IDLE"}
          </span>
        </div>
      </div>

      <div class="mt-4 grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div class="bg-white border border-gray-200 rounded-lg p-4">
          <div class="text-xs text-gray-500">Job ID</div>
          <div class="font-mono text-sm text-gray-900 mt-1 break-all">{jobId ?? "—"}</div>
        </div>

        <div class="bg-white border border-gray-200 rounded-lg p-4">
          <div class="text-xs text-gray-500">Status</div>
          <div class="text-sm font-semibold mt-1">
            {#if status?.status === "succeeded"}
              <span class="text-green-700">SUCCEEDED</span>
            {:else if status?.status === "failed"}
              <span class="text-red-700">FAILED</span>
            {:else if status?.status === "running"}
              <span class="text-blue-700">RUNNING</span>
            {:else}
              <span class="text-gray-600">—</span>
            {/if}
          </div>
        </div>

        <div class="bg-white border border-gray-200 rounded-lg p-4">
          <div class="text-xs text-gray-500">Type</div>
          <div class="text-sm font-semibold text-gray-900 mt-1">{status?.type ?? "—"}</div>
        </div>
      </div>

      <div class="mt-5">
        <pre
          bind:this={logsEl}
          class="bg-black text-green-200 rounded-lg p-4 text-xs leading-relaxed overflow-auto border border-gray-800"
          style="max-height: 520px;"
        >{logs || "No logs yet."}</pre>
      </div>
    </div>
  </section>
</div>
