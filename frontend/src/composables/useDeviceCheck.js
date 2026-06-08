import { ref, nextTick } from 'vue'

export function useDeviceCheck() {
  const videoEl = ref(null)
  const camStatus = ref('pending')
  const micStatus = ref('pending')
  const speakerStatus = ref('pending')
  const camOn = ref(true)
  const micOn = ref(true)
  const volumeLevel = ref(0)
  const camLabel = ref('카메라')
  const micLabel = ref('마이크')

  let mediaStream = null
  let audioCtx = null
  let analyserNode = null
  let animFrameId = null

  async function startDeviceCheck() {
    camStatus.value = 'pending'
    micStatus.value = 'pending'
    speakerStatus.value = 'pending'
    volumeLevel.value = 0
    camOn.value = true
    micOn.value = true

    try {
      mediaStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })

      const devices = await navigator.mediaDevices.enumerateDevices()
      const camDev = devices.find(d => d.kind === 'videoinput')
      const micDev = devices.find(d => d.kind === 'audioinput')
      if (camDev?.label) camLabel.value = camDev.label
      if (micDev?.label) micLabel.value = micDev.label

      await nextTick()
      if (videoEl.value) videoEl.value.srcObject = mediaStream
      camStatus.value = 'ok'

      startVolumeMeter(mediaStream)
    } catch {
      camStatus.value = 'error'
      micStatus.value = 'error'
    }
  }

  function startVolumeMeter(stream) {
    try {
      audioCtx = new AudioContext()
      analyserNode = audioCtx.createAnalyser()
      analyserNode.fftSize = 256
      audioCtx.createMediaStreamSource(stream).connect(analyserNode)
      const buf = new Uint8Array(analyserNode.frequencyBinCount)

      function tick() {
        analyserNode.getByteFrequencyData(buf)
        const avg = buf.reduce((a, b) => a + b, 0) / buf.length
        volumeLevel.value = Math.min(100, Math.round(avg * 2.5))
        if (avg > 3) micStatus.value = 'ok'
        animFrameId = requestAnimationFrame(tick)
      }
      tick()
    } catch {
      micStatus.value = 'error'
    }
  }

  function stopDeviceCheck() {
    if (animFrameId) { cancelAnimationFrame(animFrameId); animFrameId = null }
    if (analyserNode) { analyserNode.disconnect(); analyserNode = null }
    if (audioCtx) { audioCtx.close(); audioCtx = null }
    if (mediaStream) { mediaStream.getTracks().forEach(t => t.stop()); mediaStream = null }
    volumeLevel.value = 0
  }

  function toggleCam() {
    if (!mediaStream) return
    camOn.value = !camOn.value
    mediaStream.getVideoTracks().forEach(t => { t.enabled = camOn.value })
  }

  function toggleMic() {
    if (!mediaStream) return
    micOn.value = !micOn.value
    mediaStream.getAudioTracks().forEach(t => { t.enabled = micOn.value })
  }

  function testSpeaker() {
    try {
      const ctx = new AudioContext()
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.type = 'sine'
      osc.frequency.value = 440
      gain.gain.setValueAtTime(0.3, ctx.currentTime)
      gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 1.5)
      osc.start(ctx.currentTime)
      osc.stop(ctx.currentTime + 1.5)
      osc.onended = () => { ctx.close(); speakerStatus.value = 'ok' }
    } catch {
      speakerStatus.value = 'error'
    }
  }

  return {
    videoEl, camStatus, micStatus, speakerStatus,
    camOn, micOn, volumeLevel, camLabel, micLabel,
    startDeviceCheck, stopDeviceCheck,
    toggleCam, toggleMic, testSpeaker,
  }
}
