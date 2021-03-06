/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package flash.display;

import flash.utils.ByteArray;
import jitb.lang.annotations.Element;
import jitb.lang.annotations.Metadata;
import jitb.util.TextureUtil;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL13;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * @author Joa Ebert
 */
@Metadata({@Element(name="Version", keys={""}, values={"10"})})
public final class ShaderInput extends jitb.lang.Object {
	public static ShaderInput JITB$create(final String name, final int channels, final int index) {
		return new ShaderInput(name, channels, index);
	}

	private enum InputType { NONE, BITMAPDATA, BYTEARRAY, VECTOR }
	private final HashMap<String, Object> _dynamic = new HashMap<String, Object>();

	private String _name;
	private int _channels;
	private int _height;
	private int _index;
	private jitb.lang.Object _input;
	private int _width;
	private InputType _inputType = InputType.NONE;

	public ShaderInput() {}

	private ShaderInput(final String name, final int channels, final int index) {
		_name = name;
		_channels = channels;
		_index = index;
	}

	String name() {
		return _name;
	}

	public int channels() { return _channels; }

	public int height() { return _height; }
	public void height(final int value) { _height = value; }

	public int index() { return _index; }

	public void input(final jitb.lang.Object value) {
		_input = value;

		if(_input == null) {
			_inputType = InputType.NONE;
		} else {
			if(_input instanceof BitmapData) {
				final BitmapData bitmapData = (BitmapData)_input;
				width(bitmapData.width());
				height(bitmapData.height());
				_inputType = InputType.BITMAPDATA;
			} else if(_input instanceof ByteArray) {
				final ByteArray byteArray = (ByteArray)_input;
				if((byteArray.length() % channels()) != 0) {
					throw new Error("ByteArray length is not a divisible by "+channels()+".");
				}
				_inputType = InputType.BYTEARRAY;
			/*} else if(_input instanceof Vector) {
				_inputType = InputType.VECTOR;
			*/
			} else {
				throw new Error("Illegal input type.");//todo replace with correct argument error.
			}
		}
	}
	public jitb.lang.Object input() { return _input; }

	public int width() { return _width; }
	public void width(final int value) { _width = value; }

	@Override
	public Object JITB$getProperty(final String property) {
		if(property.equals("channels")) {
			return channels();
		} else if(property.equals("height")) {
			return height();
		} else if(property.equals("index")) {
			return index();
		} else if(property.equals("input")) {
			return input();
		} else if(property.equals("width")) {
			return width();
		} else {
			return _dynamic.get(property);
		}
	}

	@Override
	public void JITB$setProperty(final String property, final Object value) {
		if(property.equals("channels")) {
			throw new IllegalAccessError();//this is read-only
		} else if(property.equals("height")) {
			height((Integer)value);
		} else if(property.equals("index")) {
			throw new IllegalAccessError();//this is read-only
		} else if(property.equals("input")) {
			input((jitb.lang.Object)value);
		} else if(property.equals("width")) {
			height((Integer)value);
		} else {
			_dynamic.put(property, value);
		}
	}

	public void JITB$applyInput(final int programId) {
		if(null != input() && InputType.NONE.equals(_inputType)) {
			return;
		}

		final int textureLocation = ARBShaderObjects.glGetUniformLocationARB(programId, "tex"+index());
		ARBShaderObjects.glUniform1iARB(textureLocation, index());

		switch(_inputType) {
			case BITMAPDATA:
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + index());
				glBindTexture(TextureUtil.mode(), ((BitmapData)input()).JITB$textureId());
				break;

			default:
				//
				// System.out.println("Warning: Unhandled input type "+_inputType+".");
				//
		}
	}

	public void JITB$unapplyInput() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + index());
		glBindTexture(TextureUtil.mode(), 0);
	}
}
