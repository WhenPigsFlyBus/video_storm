/*
 * Copyright (c) 2012, Riven
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Riven nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.indiespot.media.impl;

import static org.lwjgl.openal.AL10.*;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.indiespot.media.AudioRenderer;
import net.indiespot.media.Movie;

import craterstudio.io.Streams;
import craterstudio.math.EasyMath;
import craterstudio.util.concur.SimpleBlockingQueue;

public class OpenALAudioRenderer extends AudioRenderer {

	private int lastBuffersProcessed = 0;
	private boolean hasMoreSamples = true;

	private static enum ActionType {
		ADJUST_VOLUME, PAUSE_AUDIO, RESUME_AUDIO
	}

	private static enum State {
		INIT, BUFFERING, PLAYING, PAUSED, CLOSED;
	}

	private static class Action {
		final ActionType type;
		final Object value;

		public Action(ActionType type, Object value) {
			this.type = type;
			this.value = value;
		}

		public float floatValue() {
			return ((Float) value).floatValue();
		}
	}

	final SimpleBlockingQueue<Action> pendingActions = new SimpleBlockingQueue<>();

	@Override
	public void setVolume(float volume) {
		if (!EasyMath.isBetween(volume, 0.0f, 1.0f)) {
			throw new IllegalArgumentException();
		}
		pendingActions.put(new Action(ActionType.ADJUST_VOLUME, Float.valueOf(volume)));
	}

	@Override
	public void pause() {
		pendingActions.put(new Action(ActionType.PAUSE_AUDIO, null));
	}

	@Override
	public void resume() {
		pendingActions.put(new Action(ActionType.RESUME_AUDIO, null));
	}

	private State state = State.INIT;
	private int alSource;

	private void init() {
		this.alSource = alGenSources();
		if (this.alSource == 0) {
			throw new IllegalStateException();
		}
	}

	private void buffer() {
		System.out.println("Audio buffering...");

		// buffer 1sec of audio
		for (int i = 0; i < frameRate; i++) {
			this.enqueueNextSamples();
		}

		alSourcePlay(alSource);
	}

	@SuppressWarnings("incomplete-switch")
	public boolean tick(Movie sync) {

		switch (this.state) {
			case INIT:
				this.init();
				this.state = State.BUFFERING;
				return true;

			case BUFFERING:
				this.buffer();
				this.state = State.PLAYING;
				return true;

			case CLOSED:
				return false;
		}

		if (alSource == 0) {
			throw new IllegalStateException();
		}

		/*
		 * Handle pending actions
		 */

		for (Action action; (action = pendingActions.poll()) != null;) {
			switch (action.type) {
				case ADJUST_VOLUME:
					alSourcef(alSource, AL_GAIN, action.floatValue());
					break;

				case PAUSE_AUDIO:
					System.out.println("pausing...");
					alSourcePause(alSource);
					state = State.PAUSED;
					return true;

				case RESUME_AUDIO:
					System.out.println("resuming...");
					alSourcePlay(alSource);
					state = State.PLAYING;
					break;

				default:
					throw new IllegalStateException();
			}
		}

		switch (this.state) {
			case PLAYING:
				break;

			case PAUSED:
				return true;

			default:
				throw new IllegalStateException();
		}

		int currentBuffersProcessed = alGetSourcei(alSource, AL_BUFFERS_PROCESSED);
		int toDiscard = currentBuffersProcessed - lastBuffersProcessed;
		lastBuffersProcessed = currentBuffersProcessed;

		if (toDiscard == 0) {
			return true;
		}
		if (toDiscard < 0) {
			throw new IllegalStateException();
		}

		for (int i = 0; i < toDiscard; i++) {
			int buffer = alSourceUnqueueBuffers(alSource);
			alDeleteBuffers(buffer);

			this.lastBuffersProcessed--;
			sync.onRenderedAudioBuffer();

			this.enqueueNextSamples();
		}

		switch (alGetSourcei(alSource, AL_SOURCE_STATE)) {
			case AL_PLAYING:
				// ok
				break;

			case AL_STOPPED:
				if (this.lastBuffersProcessed != 0) {
					throw new IllegalStateException("should never happen");
				}
				if (this.hasMoreSamples) {
					this.state = State.BUFFERING;
				} else {
					sync.onEndOfAudio();
					Streams.safeClose(this);
					return false;
				}
				break;

			default:
				throw new IllegalStateException("unexpected state");
		}

		return true;
	}

	private void enqueueNextSamples() {

		if (!this.hasMoreSamples) {
			return;
		}

		ByteBuffer samples = super.loadNextSamples();
		if (samples == null) {
			this.hasMoreSamples = false;
			return;
		}

		int buffer = alGenBuffers();
		alBufferData(buffer, AL_FORMAT_STEREO16, samples, audioStream.sampleRate);
		alSourceQueueBuffers(this.alSource, buffer);
	}

	public void close() throws IOException {
		if (this.alSource != 0) {
			alSourceStop(this.alSource);
			alDeleteSources(this.alSource);
			this.alSource = 0;
			this.state = State.CLOSED;
		}

		super.close();
	}
}