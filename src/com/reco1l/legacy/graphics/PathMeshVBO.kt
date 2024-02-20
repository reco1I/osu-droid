package com.reco1l.legacy.graphics

import android.opengl.GLES20
import com.reco1l.legacy.graphics.PathMesh.Companion.MESH_3D_ATTRIBUTES
import org.andengine.entity.primitive.Mesh
import org.andengine.entity.primitive.vbo.IMeshVertexBufferObject
import org.andengine.opengl.vbo.DrawType.STATIC
import org.andengine.opengl.vbo.VertexBufferObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PathMeshVBO :
    VertexBufferObject(null, 0, STATIC, true, MESH_3D_ATTRIBUTES),
    IMeshVertexBufferObject
{

    private var bufferData = FloatArray(0)

    private var floatBuffer = mByteBuffer.asFloatBuffer()

    private var alpha = 0f

    private var isAlphaChanged = false


    override fun getHeapMemoryByteSize() = byteCapacity

    override fun getNativeHeapMemoryByteSize() = byteCapacity



    override fun getBufferData() = bufferData

    fun setBufferData(data: FloatArray)
    {
        bufferData = data
        setDirtyOnHardware()
    }


    override fun onUpdateVertices(mesh: Mesh) = setDirtyOnHardware()

    override fun onUpdateColor(mesh: Mesh)
    {
        // We update the alpha when the data is binded to the hardware buffer so we ensure it is done
        // after the data is updated.
        if (alpha != mesh.alpha)
        {
            alpha = mesh.alpha
            isAlphaChanged = true
            setDirtyOnHardware()
        }
    }

    override fun onBufferData()
    {
        val bufferData = bufferData
        mCapacity = bufferData.size

        if (isAlphaChanged)
        {
            isAlphaChanged = false
            // This will only update the alpha since is expected to the color be specified at every
            // vertex due to the attributes.
            val alpha = (255f * alpha).toInt()

            for (i in 0 until bufferData.size / 4)
            {
                val color = bufferData[i * 4 + 3].toRawBits()

                // Source color with alpha applied.
                val newColor = (alpha shl 24)
                    .or(color shr 16 and 0xFF shl 16)
                    .or(color shr 8 and 0xFF shl 8)
                    .or(color shr 0 and 0xFF shl 0)

                bufferData[i * 4 + 3] = Float.fromBits(newColor and -0x1)
            }
        }


        if (floatBuffer.capacity() < capacity)
        {
            mByteBuffer = ByteBuffer.allocateDirect((capacity + 24) * Float.SIZE_BYTES)
            mByteBuffer.order(ByteOrder.nativeOrder())

            floatBuffer = mByteBuffer.asFloatBuffer()
        }

        floatBuffer.position(0)
        floatBuffer.put(bufferData)

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mByteBuffer.capacity(), mByteBuffer, mUsage)
    }

}