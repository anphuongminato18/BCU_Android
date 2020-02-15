package com.mandarin.bcu.androidutil

import android.util.SparseArray
import androidx.core.util.isNotEmpty
import com.mandarin.bcu.androidutil.io.ErrorLogWriter
import common.util.stage.MapColc
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.unit.Enemy

object FilterStage {
    fun setFilter(enemies: List<Int>, enemorand: Boolean, music: Int, bg: Int, star: Int, bh: Int, bhop: Int, contin: Int, boss: Int) : SparseArray<SparseArray<ArrayList<Int>>> {
        val result = SparseArray<SparseArray<ArrayList<Int>>>()

        val mc = MapColc.MAPS ?: return result

        for(i in StaticStore.MAPCODE) {
            val m = mc[i] ?: continue

            val stresult = SparseArray<ArrayList<Int>>()

            for(j in m.maps.indices) {
                val stm = m.maps[j] ?: continue

                val sresult = ArrayList<Int>()

                for(k in 0 until stm.list.size) {
                    val s = stm.list[k] ?: continue

                    val enem = containEnemy(enemies, s.data.allEnemy, enemorand)

                    val mus = s.mus0 == music || s.mus1 == music || music == -1

                    val backg = s.bg == bg || bg == -1

                    val stars = stm.stars.size > star

                    val baseh = if(bh != -1) {
                        when(bhop) {
                            -1 -> true
                            0 -> s.health < bh
                            1 -> s.health == bh
                            2 -> s.health > bh
                            else -> false
                        }
                    } else {
                        true
                    }

                    val cont = when(contin) {
                        -1 -> true
                        0 -> !s.non_con
                        1 -> s.non_con
                        else -> false
                    }

                    val bos = when(boss) {
                        -1 -> true
                        0 -> hasBoss(s)
                        1 -> !hasBoss(s)
                        else -> false
                    }

                    if(enem && mus && backg && stars && baseh && cont && bos)
                        sresult.add(k)
                }

                if(sresult.isNotEmpty())
                    stresult.put(j,sresult)
            }

            if(stresult.isNotEmpty())
                result.put(i,stresult)
        }

        return result
    }

    private fun containEnemy(src: List<Int>, target: Set<Enemy>, orand: Boolean) : Boolean {
        if(src.isEmpty()) return true

        if(target.isEmpty()) return false

        val targetid = ArrayList<Int>()

        for(ten in target) {
            if (!targetid.contains(ten.id)) {
                targetid.add(ten.id)
            }
        }

        //True for Or search

        if(orand) {
            for(i in src) {
                if(targetid.contains(i))
                    return true
            }

            return false
        } else {
            return targetid.containsAll(src)
        }
    }

    private fun hasBoss(st: Stage) : Boolean {
        try {
            val def = st.data ?: return false

            for (i in def.datas) {
                if (i[SCDef.B] == 1)
                    return true
            }
        } catch(e: Exception) {
            ErrorLogWriter.writeLog(e, StaticStore.upload)
            return false
        }

        return false
    }
}