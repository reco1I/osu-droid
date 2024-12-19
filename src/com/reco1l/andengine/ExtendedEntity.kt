package com.reco1l.andengine

import android.util.*
import com.reco1l.andengine.container.*
import com.reco1l.andengine.modifier.*
import com.reco1l.framework.*
import org.anddev.andengine.engine.camera.*
import org.anddev.andengine.entity.*
import org.anddev.andengine.entity.scene.CameraScene
import org.anddev.andengine.entity.scene.Scene
import org.anddev.andengine.entity.shape.*
import org.anddev.andengine.opengl.util.*
import org.anddev.andengine.opengl.vertex.*
import org.anddev.andengine.util.Transformation
import javax.microedition.khronos.opengles.*
import javax.microedition.khronos.opengles.GL10.*


/**
 * Entity with extended features.
 *
 * @author Reco1l
 */
abstract class ExtendedEntity(

    private var vertexBuffer: VertexBuffer? = null

) : Shape(0f, 0f), IModifierChain {

    /**
     * Determines which axes the entity should automatically adjust its size to.
     *
     * In this case the size will equal to the content size of the entity. Some
     * types of entities requieres the user to manually set the size, in those
     * cases this property might be ignored.
     */
    open var autoSizeAxes = Axes.None
        set(value) {
            if (field != value) {
                field = value

                // Setting the opposite value for relativeSizeAxes to avoid conflicts.
                if (relativeSizeAxes != Axes.None) {

                    if (value == Axes.Both) {
                        relativeSizeAxes = Axes.None
                    }

                    if (value == Axes.X && relativeSizeAxes == Axes.Y) {
                        relativeSizeAxes = Axes.Y
                    }

                    if (value == Axes.Y && relativeSizeAxes == Axes.X) {
                        relativeSizeAxes = Axes.X
                    }
                }

                onContentSizeMeasured()
            }
        }

    /**
     * Determines which axes the entity should adjust its size relative to its parent.
     *
     * Depending on the type, the entity's width and height will be taken as a multiplier
     * for the parent's width and height in order to calculate the final size.
     *
     * Example:
     *
     * If relativeSizeAxes is set to [Axes.Both] and we set the size to 0.5, the entity's
     * size will be half the size of the parent.
     *
     * Note: autoSizeAxes has priority over relativeSizeAxes. As for example if autoSizeAxes
     * is set to [Axes.Both] and relativeSizeAxes is set to [Axes.Both], relativeSizeAxes
     * will be ignored.
     */
    open var relativeSizeAxes = Axes.None
        set(value) {
            if (field != value) {
                field = value

                // Setting the opposite value for autoSizeAxes to avoid conflicts.
                if (autoSizeAxes != Axes.None) {

                    if (value == Axes.Both) {
                        autoSizeAxes = Axes.None
                    }

                    if (value == Axes.X && autoSizeAxes == Axes.Y) {
                        autoSizeAxes = Axes.Y
                    }

                    if (value == Axes.Y && autoSizeAxes == Axes.X) {
                        autoSizeAxes = Axes.X
                    }
                }

                onContentSizeMeasured()
            }
        }

    /**
     * Determines which axes the entity should adjust its position relative to its parent.
     *
     * Depending on the type, the entity's position will be taken as a multiplier applied to
     * the parent's width and height in order to calculate the final position.
     *
     * Example:
     *
     * If relativePositionAxes is set to [Axes.Both] and we set the position to 0.5 for both axes,
     * the entity's position will be at the center of the parent.
     */
    open var relativePositionAxes = Axes.None
        set(value) {
            if (field != value) {
                field = value
                invalidateTransformations()
            }
        }

    /**
     * The origin factor of the entity in the X axis.
     */
    open var originX = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidateTransformations()
            }
        }

    /**
     * The origin factor of the entity in the Y axis.
     */
    open var originY = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidateTransformations()
            }
        }

    /**
     * The anchor factor of the entity in the X axis.
     * This is used to determine the position of the entity in a container.
     *
     * Note: This will not take effect if the entity is not a child of a [Container].
     */
    open var anchorX = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidateTransformations()
            }
        }

    /**
     * The anchor factor of the entity in the Y axis.
     * This is used to determine the position of the entity in a container.
     *
     * Note: This will not take effect if the entity is not a child of a [Container].
     */
    open var anchorY = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidateTransformations()
            }
        }

    /**
     * The translation in the X axis.
     */
    open var translationX = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidateTransformations()
            }
        }

    /**
     * The translation in the Y axis.
     */
    open var translationY = 0f
        set(value) {
            if (field != value) {
                field = value
                invalidateTransformations()
            }
        }

    /**
     * Whether the color should be inherited from all the parents in the hierarchy.
     */
    open var inheritColor = true

    /**
     * Whether the depth buffer should be cleared before drawing the entity.
     * This is useful when the entity is drawn on top of other entities by overlapping them.
     *
     * It will only take effect if the entities on the front have the depth buffer test enabled.
     *
     * @see [testWithDepthBuffer]
     */
    open var clearDepthBufferBeforeDraw = false

    /**
     * Whether the entity should be tested with the depth buffer.
     */
    open var testWithDepthBuffer = false

    /**
     * The color of the entity boxed in a [ColorARGB] object.
     */
    open var color: ColorARGB
        get() = ColorARGB(mRed, mGreen, mBlue, mAlpha)
        set(value) {
            mRed = value.red
            mGreen = value.green
            mBlue = value.blue
            mAlpha = value.alpha
        }

    /**
     * The color blending function.
     */
    open var blendingFunction: BlendingFunction? = null
        set(value) {
            if (field != value) {
                field = value

                if (value != null) {
                    mSourceBlendFunction = value.source
                    mDestinationBlendFunction = value.destination
                }
            }
        }

    /**
     * The width of the content inside the entity.
     */
    open var contentWidth = 0f
        protected set(value) {
            if (field != value) {
                field = value
                onContentSizeMeasured()
            }
        }

    /**
     * The height of the content inside the entity.
     */
    open var contentHeight = 0f
        protected set(value) {
            if (field != value) {
                field = value
                onContentSizeMeasured()
            }
        }

    /**
     * The real width of the entity in pixels.
     *
     * Due to compatibility reason, this doesn't take into account transformations like rotation or scaling.
     * @see [getWidthScaled]
     */
    open val drawWidth: Float
        get() {
            if (relativeSizeAxes.isHorizontal) {
                return getParentWidth() * width
            }
            return width
        }

    /**
     * The real height of the entity in pixels.
     *
     * Due to compatibility reason, this doesn't take into account transformations like rotation or scaling.
     * @see [getHeightScaled]
     */
    open val drawHeight: Float
        get() {
            if (relativeSizeAxes.isVertical) {
                return getParentHeight() * height
            }
            return height
        }

    /**
     * The raw X position of the entity.
     * This is the position without taking into account the origin, anchor, or translation.
     */
    open val drawX: Float
        get() {
            val parent = parent
            if (parent is Container) {
                return parent.getChildDrawX(this)
            }

            if (relativePositionAxes.isHorizontal) {
                return getParentWidth() * x + totalOffsetX
            }

            return x + totalOffsetX
        }


    /**
     * The raw Y position of the entity.
     * This is the position without taking into account the origin, anchor, or translation.
     */
    open val drawY: Float
        get() {
            val parent = parent
            if (parent is Container) {
                return parent.getChildDrawY(this)
            }

            if (relativePositionAxes.isVertical) {
                return getParentHeight() * y + totalOffsetY
            }

            return y + totalOffsetY
        }

    /**
     * The offset applied to the X axis according to the origin factor.
     */
    open val originOffsetX: Float
        get() = -(drawWidth * originX)

    /**
     * The offset applied to the Y axis according to the origin factor.
     */
    open val originOffsetY: Float
        get() = -(drawHeight * originY)

    /**
     * The offset applied to the X axis according to the anchor factor.
     */
    open val anchorOffsetX: Float
        get() = getParentWidth() * anchorX

    /**
     * The offset applied to the Y axis according to the anchor factor.
     */
    open val anchorOffsetY: Float
        get() = getParentHeight() * anchorY

    /**
     * The total offset applied to the X axis.
     */
    open val totalOffsetX
        get() = originOffsetX + anchorOffsetX + translationX

    /**
     * The total offset applied to the Y axis.
     */
    open val totalOffsetY
        get() = originOffsetY + anchorOffsetY + translationY


    private var width = 0f

    private var height = 0f

    private var isVertexBufferDirty = true


    // Attachment

    override fun setParent(pEntity: IEntity?) {
        (parent as? Scene)?.unregisterTouchArea(this)
        super.setParent(pEntity)
        (pEntity as? ExtendedScene)?.registerTouchArea(this)
    }


    // Positions

    open fun setAnchor(anchor: Anchor) {
        anchorX = anchor.factorX
        anchorY = anchor.factorY
    }

    open fun setOrigin(origin: Anchor) {
        originX = origin.factorX
        originY = origin.factorY
    }

    override fun setPosition(x: Float, y: Float) {
        if (mX != x || mY != y) {
            mX = x
            mY = y
            invalidateTransformations()
            (parent as? Container)?.onChildPositionChanged(this)
        }
    }

    open fun setX(value: Float) {
        if (mX != value) {
            mX = value
            invalidateTransformations()
            (parent as? Container)?.onChildPositionChanged(this)
        }
    }

    open fun setY(value: Float) {
        if (mY != value) {
            mY = value
            invalidateTransformations()
            (parent as? Container)?.onChildPositionChanged(this)
        }
    }

    open fun setTranslation(x: Float, y: Float) {
        if (translationX != x || translationY != y) {
            translationX = x
            translationY = y
            invalidateTransformations()
        }
    }

    open fun invalidateTransformations() {
        mLocalToParentTransformationDirty = true
        mParentToLocalTransformationDirty = true
    }


    // Drawing

    override fun applyTranslation(pGL: GL10, camera: Camera) {

        val drawX = this.drawX
        val drawY = this.drawY

        if (drawX != 0f || drawY != 0f) {
            pGL.glTranslatef(drawX, drawY, 0f)
        }
    }

    override fun applyRotation(pGL: GL10) {

        // This will ensure getSceneCenterCoordinates() applies the correct transformation.
        mRotationCenterX = drawWidth * originX
        mRotationCenterY = drawHeight * originY

        if (rotation != 0f) {
            pGL.glRotatef(rotation, 0f, 0f, 1f)
        }
    }

    override fun applyScale(pGL: GL10) {

        // This will ensure getSceneCenterCoordinates() applies the correct transformation.
        mScaleCenterX = drawWidth * originX
        mScaleCenterY = drawHeight * originY

        if (scaleX != 1f || scaleY != 1f) {
            pGL.glScalef(scaleX, scaleY, 1f)
        }
    }

    protected open fun applyColor(pGL: GL10) {

        var red = mRed
        var green = mGreen
        var blue = mBlue
        var alpha = mAlpha

        if (inheritColor) {

            var parent = parent
            while (parent != null) {

                red *= parent.red
                green *= parent.green
                blue *= parent.blue
                alpha *= parent.alpha

                // We'll assume at this point there's no need to keep multiplying.
                if (red == 0f && green == 0f && blue == 0f && alpha == 0f) {
                    break
                }

                parent = parent.parent
            }
        }

        GLHelper.setColor(pGL, red, green, blue, alpha)
    }

    protected open fun applyBlending(pGL: GL10) {

        // If there's a blending function set, apply it instead of the engine's method.
        val blendingFunction = blendingFunction

        if (blendingFunction != null) {

            val parent = parent as? ExtendedEntity

            // If the blending function is set to inherit, apply the parent's blending function.
            if (blendingFunction == BlendingFunction.Inherit && parent != null) {
                GLHelper.blendFunction(pGL, parent.mSourceBlendFunction, parent.mDestinationBlendFunction)
            } else {
                GLHelper.blendFunction(pGL, blendingFunction.source, blendingFunction.destination)
            }

        } else {
            GLHelper.blendFunction(pGL, mSourceBlendFunction, mDestinationBlendFunction)
        }
    }

    override fun onApplyTransformations(pGL: GL10, camera: Camera) {
        applyTranslation(pGL, camera)

        if (rotation != 0f || scaleX != 1f || scaleY != 1f) {
            pGL.glTranslatef(-originOffsetX, -originOffsetY, 0f)
            applyRotation(pGL)
            applyScale(pGL)
            pGL.glTranslatef(originOffsetX, originOffsetY, 0f)
        }

        applyColor(pGL)
        applyBlending(pGL)
    }


    override fun onManagedUpdate(pSecondsElapsed: Float) {

        if (isVertexBufferDirty) {
            isVertexBufferDirty = false
            onUpdateVertexBuffer()
        }

        super.onManagedUpdate(pSecondsElapsed)
    }

    override fun onInitDraw(pGL: GL10) {

        if (vertexBuffer != null) {
            GLHelper.enableVertexArray(pGL)
        }

        if (clearDepthBufferBeforeDraw) {
            pGL.glClear(GL_DEPTH_BUFFER_BIT)
        }

        GLHelper.setDepthTest(pGL, testWithDepthBuffer)
    }

    override fun onApplyVertices(pGL: GL10) {
        if (vertexBuffer != null) {
            super.onApplyVertices(pGL)
        }
    }


    // Vertex buffer

    override fun updateVertexBuffer() {
        isVertexBufferDirty = true
    }

    fun updateVertexBufferNow() {
        isVertexBufferDirty = false
        onUpdateVertexBuffer()
    }

    /**
     * Sets the vertex buffer of the entity.
     *
     * Note: This will unload the previous buffer from the active buffer object manager if it's managed.
     * If it's not managed you will have to manually unload it otherwise it will cause a memory leak.
     */
    fun setVertexBuffer(buffer: VertexBuffer) {
        vertexBuffer?.unloadFromActiveBufferObjectManager()
        vertexBuffer = buffer
        updateVertexBuffer()
    }

    override fun getVertexBuffer(): VertexBuffer? {
        return vertexBuffer
    }


    // Size

    /**
     * Called when the content size is measured.
     *
     * @return Whether the size of the entity was changed or not, this depends on the [autoSizeAxes] property.
     */
    open fun onContentSizeMeasured(): Boolean {

        if (autoSizeAxes == Axes.None) {
            return false
        }

        if (contentWidth != width || contentHeight != height) {

            if (autoSizeAxes.isHorizontal) {
                width = if (relativeSizeAxes.isHorizontal) contentWidth / getParentWidth() else contentWidth
            }

            if (autoSizeAxes.isVertical) {
                height = if (relativeSizeAxes.isVertical) contentHeight / getParentHeight() else contentHeight
            }

            updateVertexBuffer()

            (parent as? Container)?.onChildSizeChanged(this)
            return true
        }
        return false
    }

    /**
     * Sets the size of the entity.
     *
     * @return Whether the size of the entity was changed or not, this depends on the [autoSizeAxes] property.
     */
    open fun setSize(newWidth: Float, newHeight: Float): Boolean {

        if (autoSizeAxes == Axes.Both) {
            throw IllegalArgumentException("Cannot set size when autoSizeAxes is set to Both.")
        }

        if (width != newWidth || height != newHeight) {

            if (!autoSizeAxes.isHorizontal) {
                width = newWidth
            }

            if (!autoSizeAxes.isVertical) {
                height = newHeight
            }

            updateVertexBuffer()
            (parent as? Container)?.onChildSizeChanged(this)
            return true
        }
        return false
    }

    open fun setWidth(value: Float) {

        if (autoSizeAxes.isHorizontal) {
            throw IllegalArgumentException("Cannot set width when autoSizeAxes is set to Both or X.")
        }

        if (width != value) {
            width = value

            updateVertexBuffer()
            (parent as? Container)?.onChildSizeChanged(this)
        }
    }

    open fun setHeight(value: Float) {

        if (autoSizeAxes.isVertical) {
            throw IllegalArgumentException("Cannot set height when autoSizeAxes is set to Both or Y.")
        }

        if (height != value) {
            height = value

            updateVertexBuffer()
            (parent as? Container)?.onChildSizeChanged(this)
        }
    }

    override fun getWidth(): Float {
        return width
    }

    override fun getHeight(): Float {
        return height
    }

    override fun getWidthScaled(): Float {
        return drawWidth * scaleX
    }

    override fun getHeightScaled(): Float {
        return drawHeight * scaleY
    }


    // Unsupported methods

    @Deprecated("Base width is not preserved in ExtendedEntity, use getWidth() instead.")
    override fun getBaseWidth() = width

    @Deprecated("Base height is not preserved in ExtendedEntity, use getHeight() instead.")
    override fun getBaseHeight() = height

    @Deprecated("Rotation center is determined by the entity's origin, use setOrigin() instead.")
    final override fun setRotationCenter(pRotationCenterX: Float, pRotationCenterY: Float) {}

    @Deprecated("Rotation center is determined by the entity's origin, use setOrigin() instead.")
    final override fun setRotationCenterX(pRotationCenterX: Float) {}

    @Deprecated("Rotation center is determined by the entity's origin, use setOrigin() instead.")
    final override fun setRotationCenterY(pRotationCenterY: Float) {}

    @Deprecated("Scale center is determined by the entity's origin, use setOrigin() instead.")
    final override fun setScaleCenter(pScaleCenterX: Float, pScaleCenterY: Float) {}

    @Deprecated("Scale center is determined by the entity's origin, use setOrigin() instead.")
    final override fun setScaleCenterX(pScaleCenterX: Float) {}

    @Deprecated("Scale center is determined by the entity's origin, use setOrigin() instead.")
    final override fun setScaleCenterY(pScaleCenterY: Float) {}


    // Collision

    override fun collidesWith(shape: IShape): Boolean {
        Log.w("ExtendedEntity", "Collision detection is not supported in ExtendedEntity.")
        return false
    }

    override fun contains(x: Float, y: Float): Boolean {

        if (width == 0f || height == 0f) {
            return false
        }

        return EntityCollision.contains(this, x, y)
    }

    override fun isCulled(pCamera: Camera): Boolean {
        return drawX > pCamera.maxX || drawX + drawWidth < pCamera.minX
            || drawY > pCamera.maxY || drawY + drawHeight < pCamera.minY
    }

    override fun getLocalToParentTransformation(): Transformation {

        if (mLocalToParentTransformation == null) {
            mLocalToParentTransformation = Transformation()
        }

        if (mLocalToParentTransformationDirty) {
            mLocalToParentTransformation.setToIdentity()

            if (scaleX != 1f || scaleY != 1f || rotation != 0f) {
                mLocalToParentTransformation.postTranslate(originOffsetX, originOffsetY)

                if (scaleX != 1f || scaleY != 1f) {
                    mLocalToParentTransformation.postScale(scaleX, scaleY)
                }

                if (rotation != 0f) {
                    mLocalToParentTransformation.postRotate(rotation)
                }

                mLocalToParentTransformation.postTranslate(-originOffsetX, -originOffsetY)
            }

            mLocalToParentTransformation.postTranslate(drawX, drawY)
            mLocalToParentTransformationDirty = false
        }

        return mLocalToParentTransformation
    }

    override fun getParentToLocalTransformation(): Transformation {

        if (mParentToLocalTransformation == null) {
            mParentToLocalTransformation = Transformation()
        }

        if (mParentToLocalTransformationDirty) {
            mParentToLocalTransformation.setToIdentity()
            mParentToLocalTransformation.postTranslate(-drawX, -drawY)

            if (scaleX != 1f || scaleY != 1f || rotation != 0f) {
                mParentToLocalTransformation.postTranslate(originOffsetX, originOffsetY)

                if (rotation != 0f) {
                    mParentToLocalTransformation.postRotate(-rotation)
                }

                if (scaleX != 1f || scaleY != 1f) {
                    mParentToLocalTransformation.postScale(1 / scaleX, 1 / scaleY)
                }

                mParentToLocalTransformation.postTranslate(-originOffsetX, -originOffsetY)
            }

            mParentToLocalTransformationDirty = false
        }

        return mParentToLocalTransformation
    }


    // Transformation

    override fun setBlendFunction(pSourceBlendFunction: Int, pDestinationBlendFunction: Int) {
        blendingFunction = null
        super.setBlendFunction(pSourceBlendFunction, pDestinationBlendFunction)
    }

    override fun applyModifier(block: UniversalModifier.() -> Unit): UniversalModifier {

        val modifier = UniversalModifier.GlobalPool.obtain()
        modifier.setToDefault()
        modifier.block()

        registerEntityModifier(modifier)
        return modifier
    }

}


/**
 * Returns the width of the parent entity.
 */
fun ExtendedEntity.getParentWidth() = when (val parent = parent) {
    is ExtendedEntity -> parent.drawWidth
    is CameraScene -> parent.camera.widthRaw
    is IShape -> parent.width
    else -> 0f
}

/**
 * Returns the height of the parent entity.
 */
fun ExtendedEntity.getParentHeight() = when (val parent = parent) {
    is ExtendedEntity -> parent.drawHeight
    is CameraScene -> parent.camera.heightRaw
    is IShape -> parent.height
    else -> 0f
}

/**
 * Returns the draw width of the entity.
 */
fun IEntity.getDrawWidth(): Float = when (this) {
    is ExtendedEntity -> drawWidth
    is IShape -> width
    else -> 0f
}

/**
 * Returns the draw height of the entity.
 */
fun IEntity.getDrawHeight(): Float = when (this) {
    is ExtendedEntity -> drawHeight
    is IShape -> height
    else -> 0f
}

/**
 * Returns the draw X position of the entity.
 */
fun IEntity.getDrawX(): Float = when (this) {
    is ExtendedEntity -> drawX
    is IShape -> x
    else -> 0f
}

/**
 * Returns the draw Y position of the entity.
 */
fun IEntity.getDrawY(): Float = when (this) {
    is ExtendedEntity -> drawY
    is IShape -> y
    else -> 0f
}

