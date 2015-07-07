;(ns baotask.server
;  (:use [korma.core] )
;  (:import [org.msgpack.rpc.loop EventLoop])
;  (:import [org.msgpack.rpc Server]))
;
;(def cids [50012010 50012018])
;
;(def citys ["北京" "杭州" "上海" "广州" "深圳" "深圳" "温州" "宁波" "南京" "苏州" "济南" "青岛" "大连" "无锡"
;            "合肥" "天津" "长沙" "武汉" "郑州" "石家庄" "成都" "重庆"
;            "西安" "昆明" "南宁" "福州" "厦门" "南昌" "东莞" "沈阳" "长春" "哈尔滨"
;            "安徽" "福建" "甘肃" "广东" "广西" "贵州" "海南" "河北" "河南" "湖北"
;            "湖南" "江苏" "黑龙江" "江西" "吉林" "辽宁" "内蒙古" "宁夏" "青海" "山东"
;            "山西" "陕西" "四川" "西藏" "新疆" "云南" "浙江" "香港" "澳门" "台湾"])
;(def prices [["0" "100"] ["101" "200"] ["201" "500"] ["500" "1000"] ["1001" "1000000"]])
;(def cats ["糖果包" "单肩包" "斜挎包" "手提包" "手拿包" "腰包" "胸包" "化妆包"
;          "军绿色" "天蓝色" "巧克力色" "桔色" "浅灰色" "浅绿色" "浅黄色" "深卡其布色"
;           "深灰色" "深紫色" "深蓝色" "白色" "粉红色" "紫罗兰" "紫色" "红色" "绿色" "花色"
;          "蓝色" "褐色" "透明酒红色" "黄色" "黑色"
;           "糖果" "机车" "鳄鱼纹" "拼接" "菱格" "带钻" "编织" "印花" "撞色" "花朵" "邮差"
;           "豹纹" "链条" "镂空" "蝴蝶结" "亮片" "蕾丝" "褶皱" "皮带装饰" "信封" "车缝线" "串珠"
;           "铆钉" "锁扣" "流苏" "字母" "绣花" "格纹" "压花" "织花"
;          "PU" "PU" "牛皮" "羊皮" "兔毛" "猪皮" "尼龙" "帆布" "PC" "涤纶" "牛仔布" "蛇皮" "鳄鱼皮"
;          "呢子" "无纺布" "纸质" "牛津布" "PVC" "丝绒/天鹅绒" "麻布" "草类" "丝绸"
;          "涤纶" "合成革" "棉" "尼龙" "无里布" "涤棉" "真皮"
;          "休闲/街头" "宴会" "运动" "旅行" "商务"
;          "欧美时尚" "日韩风范" "卡通可爱" "甜美淑女" "商务/OL" "民族风" "复古风" "潮酷风范"
;          "学院风" "小清新" "运动" "朋克/摇滚" "嘻哈"
;          "涤纶" "合成革" "棉" "尼龙" "无里布" "涤棉" "真皮" "纯色无图案" "文字" "花卉" "人物"
;          "动物" "几何图案" "卡通" "水果" "风景" "格子" "条纹"])
;
;
;(defn -getBaoItem [this]
;  ;(println "Start get baobao")
;  ;(doseq [price prices]
;  ;    (get-items-by-page (cats (rand-int (count cats)))
;  ;                       (cids (rand-int 2))
;  ;                       (first price)
;  ;                       (last price)
;  ;                       (citys (rand-int 30))))
;  ;(exec-raw ["delete from items where   title like '%运动背包%' or title like '%德国大众%' or title like '%电脑包%' or title like '%男%' or title like '%小野人%'or title like '%登山%'" []])
;  0)
;
;
;
;
;
;
;
;(gen-class
;  :name MathServer
;  :methods [
;            [getBaoItem [] void]])
;
;
;
;(defn -main
;  "Starts a MessagePack-RPC based server"
;  [& args]
;  (let [loop (EventLoop/defaultEventLoop)]
;    (let [svr (Server.)]
;      (.serve svr (MathServer.))
;      (.listen svr 7000))
;      (println "MathServer running and listening on 7000")
;    (.join loop)))