import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type Channel = {
  id: string
  name: string
  description: string
}

type Message = {
  id: string
  author: string
  text: string
  time: string
}

type InsightType = 'summary' | 'action-points' | 'decisions'

type Insight = {
  title: string
  subtitle: string
  content: string
  bullets: string[]
  footer: string
}

type InsightView = Insight & {
  generatedAt: string
}

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

const insightDefinitions: Array<{
  key: InsightType
  label: string
  heading: string
  emptyText: string
  helperText: string
}> = [
  {
    key: 'summary',
    label: 'Summarize',
    heading: 'Summary',
    emptyText: 'Generate a short recap for this channel.',
    helperText: 'Condense the discussion into a quick recap.',
  },
  {
    key: 'action-points',
    label: 'Action Points',
    heading: 'Action points',
    emptyText: 'Extract next steps from the conversation.',
    helperText: 'Highlight the follow-ups worth tracking.',
  },
  {
    key: 'decisions',
    label: 'Decisions',
    heading: 'Decisions made',
    emptyText: 'Identify the decisions already made in this thread.',
    helperText: 'Capture the outcomes that are already agreed.',
  },
]

async function fetchJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`)
  }

  return response.json() as Promise<T>
}

function App() {
  const [channels, setChannels] = useState<Channel[]>([])
  const [selectedChannelId, setSelectedChannelId] = useState<string>('')
  const [messages, setMessages] = useState<Message[]>([])
  const [draftMessage, setDraftMessage] = useState('')
  const [insights, setInsights] = useState<Partial<Record<InsightType, InsightView>>>({})
  const [isLoadingChannels, setIsLoadingChannels] = useState(true)
  const [isLoadingMessages, setIsLoadingMessages] = useState(false)
  const [isSending, setIsSending] = useState(false)
  const [activeInsight, setActiveInsight] = useState<InsightType | null>(null)
  const [selectedInsight, setSelectedInsight] = useState<InsightType | null>(null)
  const [error, setError] = useState<string | null>(null)
  const messageListRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    const loadChannels = async () => {
      setIsLoadingChannels(true)

      try {
        const channelData = await fetchJson<Channel[]>('/api/channels')
        setChannels(channelData)
        setSelectedChannelId((current) => current || channelData[0]?.id || '')
        setError(null)
      } catch (loadError) {
        setError('Unable to load channels from chat-service.')
      } finally {
        setIsLoadingChannels(false)
      }
    }

    void loadChannels()
  }, [])

  useEffect(() => {
    if (!selectedChannelId) {
      return
    }

    const loadMessages = async () => {
      setIsLoadingMessages(true)

      try {
        const messageData = await fetchJson<Message[]>(
          `/api/channels/${selectedChannelId}/messages`,
        )
        setMessages(messageData)
        setInsights({})
        setActiveInsight(null)
        setSelectedInsight(null)
        setError(null)
      } catch (loadError) {
        setError('Unable to load messages for this channel.')
      } finally {
        setIsLoadingMessages(false)
      }
    }

    void loadMessages()
  }, [selectedChannelId])

  useEffect(() => {
    if (!messageListRef.current) {
      return
    }

    messageListRef.current.scrollTop = messageListRef.current.scrollHeight
  }, [messages, isLoadingMessages])

  const selectedChannel =
    channels.find((channel) => channel.id === selectedChannelId) ?? null
  const selectedInsightData = selectedInsight ? insights[selectedInsight] : null

  const handleSendMessage = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const text = draftMessage.trim()
    if (!text || !selectedChannelId) {
      return
    }

    setIsSending(true)

    try {
      const createdMessage = await fetchJson<Message>(
        `/api/channels/${selectedChannelId}/messages`,
        {
          method: 'POST',
          body: JSON.stringify({ text }),
        },
      )

      setMessages((current) => [...current, createdMessage])
      setDraftMessage('')
      setError(null)
    } catch (sendError) {
      setError('Unable to send your message right now.')
    } finally {
      setIsSending(false)
    }
  }

  const handleInsight = async (type: InsightType) => {
    if (!selectedChannelId) {
      return
    }

    setSelectedInsight(type)
    setActiveInsight(type)

    try {
      const insight = await fetchJson<Insight>(
        `/api/channels/${selectedChannelId}/ai/${type}`,
        {
          method: 'POST',
        },
      )

      setInsights((current) => ({
        ...current,
        [type]: {
          ...insight,
          generatedAt: new Date().toLocaleTimeString([], {
            hour: 'numeric',
            minute: '2-digit',
          }),
        },
      }))
      setError(null)
    } catch (insightError) {
      setError('Unable to generate AI output for this channel.')
    } finally {
      setActiveInsight(null)
    }
  }

  return (
    <div className="app-shell">
      <aside className="workspace-rail">
        <div className="brand-mark">S</div>
        <div className="rail-stack">
          <button className="rail-button" type="button" aria-label="Home" />
          <button className="rail-button" type="button" aria-label="Channels" />
          <button className="rail-button" type="button" aria-label="Inbox" />
          <button className="rail-button" type="button" aria-label="Settings" />
        </div>
        <button className="rail-button rail-button--ghost" type="button" aria-label="Profile">
          Y
        </button>
      </aside>

      <aside className="channel-sidebar">
        <div className="sidebar-header">
          <h1>SyncSpace AI</h1>
        </div>

        <div className="sidebar-section">
          <p className="sidebar-label">Channels</p>
          <div className="channel-list">
            {isLoadingChannels ? (
              <p className="sidebar-note">Loading channels...</p>
            ) : (
              channels.map((channel) => (
                <button
                  key={channel.id}
                  className={`channel-item ${
                    channel.id === selectedChannelId ? 'channel-item--active' : ''
                  }`}
                  type="button"
                  onClick={() => setSelectedChannelId(channel.id)}
                >
                  <span className="channel-hash">#</span>
                  <span>{channel.name}</span>
                </button>
              ))
            )}
          </div>
        </div>

        <div className="sidebar-section sidebar-section--secondary">
          <p className="sidebar-label">Saved Views</p>
          <p className="saved-view">Recaps</p>
          <p className="saved-view">Decisions</p>
          <p className="saved-view">Action Points</p>
        </div>

        <div className="sidebar-footer">
          <div className="avatar">Y</div>
          <span>You</span>
        </div>
      </aside>

      <main className="chat-layout">
        <section className="chat-panel">
          <header className="chat-header">
            <div>
              <div className="chat-title-row">
                <span className="channel-hash">#</span>
                <h2>{selectedChannel?.name ?? 'Loading'}</h2>
              </div>
              <p>{selectedChannel?.description ?? 'Channel conversation'}</p>
            </div>

            <div className="chat-toolbar">
              <label className="search-box">
                <input aria-label="Search" placeholder="Search" />
              </label>
              <button type="button" onClick={() => void handleInsight('summary')}>
                Summarize
              </button>
            </div>
          </header>

          <div className="message-list" ref={messageListRef}>
            {isLoadingMessages ? (
              <p className="panel-note">Loading messages...</p>
            ) : (
              <>
                <div className="message-day-divider">Today</div>
                {messages.map((message) => (
                  <article key={message.id} className="message-card">
                    <div className="message-avatar">{message.author.charAt(0)}</div>
                    <div className="message-body">
                      <div className="message-meta">
                        <strong>{message.author}</strong>
                        <span>{message.time}</span>
                      </div>
                      <p>{message.text}</p>
                    </div>
                  </article>
                ))}
              </>
            )}
          </div>

          <form className="composer" onSubmit={handleSendMessage}>
            <input
              value={draftMessage}
              onChange={(event) => setDraftMessage(event.target.value)}
              placeholder={`Message #${selectedChannel?.name ?? 'channel'}`}
            />
            <button type="submit" disabled={isSending || !draftMessage.trim()}>
              {isSending ? 'Sending...' : 'Send'}
            </button>
          </form>
        </section>

        <aside className="insight-panel">
          <div className="insight-header">
            <h3>AI Assistant</h3>
            <p>Run one analysis and review the result here.</p>
          </div>

          <section className="insight-action-group">
            <p className="insight-group-label">Tools</p>

            <div className="insight-actions">
            {insightDefinitions.map((definition) => (
              <button
                key={definition.key}
                className={`insight-action ${
                  selectedInsight === definition.key ? 'insight-action--selected' : ''
                }`}
                type="button"
                onClick={() => void handleInsight(definition.key)}
                disabled={Boolean(activeInsight)}
              >
                <span className="insight-action-copy">
                  <strong>{definition.label}</strong>
                  <small>{definition.helperText}</small>
                </span>
                {activeInsight === definition.key && (
                  <span className="insight-action-status">Running</span>
                )}
              </button>
            ))}
            </div>
          </section>

          <div className="insight-results">
            <section
              className={`insight-card insight-card--primary ${
                activeInsight ? 'insight-card--loading' : ''
              }`}
            >
              {!selectedInsight && (
                <div className="insight-empty-state">
                  <h4>Assistant Output</h4>
                  <p>Choose an AI action to analyze this conversation.</p>
                </div>
              )}

              {selectedInsight && (
                <>
                  <div className="insight-card-header">
                    <div>
                      <h4>
                        {activeInsight === selectedInsight
                          ? insightDefinitions.find(
                              (definition) => definition.key === selectedInsight,
                            )?.heading ?? 'Assistant Output'
                          : selectedInsightData?.title ??
                            insightDefinitions.find(
                              (definition) => definition.key === selectedInsight,
                            )?.heading}
                      </h4>
                      <p className="insight-subtitle">
                        {activeInsight === selectedInsight
                          ? 'Reviewing the latest conversation'
                          : selectedInsightData?.subtitle ?? 'Generated insight'}
                      </p>
                    </div>
                    <p className="insight-meta">
                      {activeInsight === selectedInsight
                        ? 'Working...'
                        : selectedInsightData
                          ? `Generated at ${selectedInsightData.generatedAt}`
                          : ''}
                    </p>
                  </div>

                  {activeInsight === selectedInsight ? (
                    <div className="insight-loading-state">
                      <p>Reviewing the conversation and preparing the response...</p>
                      <div className="insight-loading-bars" aria-hidden="true">
                        <span />
                        <span />
                        <span />
                      </div>
                    </div>
                  ) : (
                    <div className="insight-body">
                      {selectedInsightData?.content && <p>{selectedInsightData.content}</p>}
                      {(selectedInsightData?.bullets.length ?? 0) > 0 && (
                        <ul>
                          {selectedInsightData?.bullets.map((bullet) => (
                            <li key={bullet}>{bullet}</li>
                          ))}
                        </ul>
                      )}
                      {selectedInsightData?.footer && (
                        <p className="insight-footer">{selectedInsightData.footer}</p>
                      )}
                    </div>
                  )}
                </>
              )}
            </section>
          </div>

          {error && <p className="error-banner">{error}</p>}
        </aside>
      </main>
    </div>
  )
}

export default App
