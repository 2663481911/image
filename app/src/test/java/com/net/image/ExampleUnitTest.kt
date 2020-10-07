package com.net.image


import com.net.image.model.convertCookie
import com.google.gson.Gson
import org.jsoup.Jsoup

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.collections.HashMap


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val cookie = "remember_82e5d2c56bdd0811318f0cf078b78bfc" +
            "=eyJpdiI6IjFXSFZCUmhBRUpEWjU3OHZ3R1RxOFE9PSIsInZhbHVlIjoiMDFpVjdWTXFKWWk5d2greGNkS05odUpROERWc1FiYTh0bGxHemRLREhwckRYbkJ4cVZ6dlNOYnNyd2VLU2RLR2wwXC9VWjFQQzlUQjhoUU9nK0daZDUyWDlSNkxDdWZJTjl1cEphNmdLR2lVPSIsIm1hYyI6IjEyY2Q3MThjMjkyMGFmZGEwMzVhNmZlOTZkYWE3Y2RlNmE2YTUzMmFiZDU0NDNlOGQzYTMyOWI1Yzg0NGI3MWYifQ%3D%3D; laravel_session=eyJpdiI6InpnMmdmYVZMMEdUaG9aV25UOE1zZ3c9PSIsInZhbHVlIjoiTSs4elRRWHFDcmVCbExvXC9FeUdyN3JNK2VSU04rUGx1RVp2YTlwMU1FQVU4c0NkbU5JK3BjMnhIaVFIMEdSV1hBcnhlS08rNTVwVVhLT3FqNXNZbFhBPT0iLCJtYWMiOiJlZjdlZTdjMDY3ZmE0OGFkZmI4MmM0YjY4ZTgyZDUzYmNmMmRiMWJhYmJiOWZmN2I1OTQ5MGZkN2I2NDQyYzEwIn0%3D"
        val convertCookie = convertCookie(cookie)
        println(convertCookie)

        val get = Jsoup.connect("http://www.cnu.cc/search/%E7%A7%81%E6%88%BF?page=2")
            .cookies(convertCookie).get()
        println(get.body().html())
    }

    fun getMap(str: String):Map<String, String>{
        val gson = Gson()
        var map: Map<String, String> = HashMap()
        map = gson.fromJson(str, map.javaClass)
        return map
    }
}

