package com.nothinglondon.sdkdemo

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import com.nothing.ketchum.GlyphMatrixManager
import com.nothing.ketchum.GlyphMatrixObject
import com.nothing.ketchum.GlyphMatrixFrame
import com.nothing.ketchum.GlyphToy

class DragonToyService : Service() {

    private var mGM: GlyphMatrixManager? = null
    private val handler = Handler(Looper.getMainLooper())

    private var isBreathingFire = false
    private var fireFrameIndex = 0

    private val messenger = Messenger(object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == GlyphToy.MSG_GLYPH_TOY) {
                val bundle = msg.data
                val event = bundle.getString(GlyphToy.MSG_GLYPH_TOY_DATA)
                
                if (GlyphToy.EVENT_CHANGE == event) {
                    if (!isBreathingFire) {
                        startFireAnimation()
                    }
                }
            }
        }
    })

    override fun onBind(intent: Intent?): IBinder? {
        initGM()
        return messenger.binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isBreathingFire = false
        handler.removeCallbacks(animateRunnable)
        mGM?.turnOff()
        mGM?.unInit()
        mGM = null
        return false
    }

    private fun initGM() {
        mGM = GlyphMatrixManager.getInstance(applicationContext)
        mGM?.init(object : GlyphMatrixManager.Callback {
            override fun onServiceConnected(componentName: ComponentName) {
                // FIXED: Passed as a String in quotes
                mGM?.register("23112")
                showIdleFrame()
            }
            override fun onServiceDisconnected(componentName: ComponentName) {}
        })
    }

    private fun showIdleFrame() {
        if (mGM == null) return
        val bitmap = createBitmapFromAscii(FRAME_IDLE)
        val obj = GlyphMatrixObject.Builder().setImageSource(bitmap).build()
        val frame = GlyphMatrixFrame.Builder().addTop(obj).build(applicationContext)
        
        // FIXED: Toy-specific hardware rendering method
        mGM?.setMatrixFrame(frame.render())
    }

    private fun startFireAnimation() {
        isBreathingFire = true
        fireFrameIndex = 0
        animateRunnable.run()
    }

    private val animateRunnable = object : Runnable {
        override fun run() {
            if (!isBreathingFire || mGM == null) return

            val asciiFrame = fireSequence[fireFrameIndex]
            val bitmap = createBitmapFromAscii(asciiFrame)
            val obj = GlyphMatrixObject.Builder().setImageSource(bitmap).build()
            val frame = GlyphMatrixFrame.Builder().addTop(obj).build(applicationContext)
            
            // FIXED: Toy-specific hardware rendering method
            mGM?.setMatrixFrame(frame.render())
            
            fireFrameIndex++
            
            if (fireFrameIndex < fireSequence.size) {
                handler.postDelayed(this, 180) 
            } else {
                isBreathingFire = false
                showIdleFrame() 
            }
        }
    }

    private fun createBitmapFromAscii(asciiFrame: Array<String>): Bitmap {
        val size = 25
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (y in 0 until size) {
            for (x in 0 until size) {
                val char = if (y < asciiFrame.size && x < asciiFrame[y].length) asciiFrame[y][x] else '.'
                val color = if (char != '.') Color.WHITE else Color.BLACK
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }

    companion object {
        val FRAME_IDLE = arrayOf(
            ".........................",
            ".........................",
            "..........###............",
            ".........#...#...........",
            ".........#..O#...........",
            "........#.....#..........",
            ".......#...##.#..........",
            "........###..#...........",
            "........#................",
            ".......#.................",
            "......#.....###..........",
            ".....#.........#.........",
            "....#...........#........",
            "...#....#.......#........",
            "...#...#........#........",
            "...#...#.......#.........",
            "....###.#######..........",
            ".......#.......#.........",
            "......#.........#........",
            "......#.........#........",
            ".....###.......###.......",
            ".........................",
            ".........................",
            ".........................",
            "........................."
        )

        val FRAME_OPEN = arrayOf(
            ".........................",
            ".........................",
            "..........###............",
            ".........#...#...........",
            ".........#..O#...........",
            "........#......#.........",
            ".......#.......#.........",
            "........####..#..........",
            "........#................",
            ".......#.................",
            "......#.....###..........",
            ".....#.........#.........",
            "....#...........#........",
            "...#....#.......#........",
            "...#...#........#........",
            "...#...#.......#.........",
            "....###.#######..........",
            ".......#.......#.........",
            "......#.........#........",
            "......#.........#........",
            ".....###.......###.......",
            ".........................",
            ".........................",
            ".........................",
            "........................."
        )

        val FRAME_FIRE_1 = arrayOf(
            ".........................",
            ".........................",
            "..........###............",
            ".........#...#...........",
            ".........#..O#...........",
            "........#......#.........",
            "....##.#.......#.........",
            "......#.####..#..........",
            "........#................",
            ".......#.................",
            "......#.....###..........",
            ".....#.........#.........",
            "....#...........#........",
            "...#....#.......#........",
            "...#...#........#........",
            "...#...#.......#.........",
            "....###.#######..........",
            ".......#.......#.........",
            "......#.........#........",
            "......#.........#........",
            ".....###.......###.......",
            ".........................",
            ".........................",
            ".........................",
            "........................."
        )

        val FRAME_FIRE_2 = arrayOf(
            ".........................",
            ".........................",
            "..........###............",
            ".........#...#...........",
            ".........#..O#...........",
            ".....#..#......#.........",
            "....#..#.......#.........",
            "...#...#.####..#.........",
            "....###.#................",
            ".......#.................",
            "......#.....###..........",
            ".....#.........#.........",
            "....#...........#........",
            "...#....#.......#........",
            "...#...#........#........",
            "...#...#.......#.........",
            "....###.#######..........",
            ".......#.......#.........",
            "......#.........#........",
            "......#.........#........",
            ".....###.......###.......",
            ".........................",
            ".........................",
            ".........................",
            "........................."
        )
        
        val fireSequence = listOf(
            FRAME_OPEN, FRAME_FIRE_1, FRAME_FIRE_2, FRAME_FIRE_1, FRAME_OPEN, FRAME_IDLE
        )
    }
}
