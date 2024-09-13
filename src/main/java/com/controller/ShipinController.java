
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 视频
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/shipin")
public class ShipinController {
    private static final Logger logger = LoggerFactory.getLogger(ShipinController.class);

    @Autowired
    private ShipinService shipinService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        params.put("shipinDeleteStart",1);params.put("shipinDeleteEnd",1);
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = shipinService.queryPage(params);

        //字典表数据转换
        List<ShipinView> list =(List<ShipinView>)page.getList();
        for(ShipinView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ShipinEntity shipin = shipinService.selectById(id);
        if(shipin !=null){
            //entity转view
            ShipinView view = new ShipinView();
            BeanUtils.copyProperties( shipin , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody ShipinEntity shipin, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,shipin:{}",this.getClass().getName(),shipin.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<ShipinEntity> queryWrapper = new EntityWrapper<ShipinEntity>()
            .eq("shipin_uuid_number", shipin.getShipinUuidNumber())
            .eq("shipin_name", shipin.getShipinName())
            .eq("shipin_video", shipin.getShipinVideo())
            .eq("shipin_types", shipin.getShipinTypes())
            .eq("zan_number", shipin.getZanNumber())
            .eq("cai_number", shipin.getCaiNumber())
            .eq("vipshipin_types", shipin.getVipshipinTypes())
            .eq("nianlingduan_types", shipin.getNianlingduanTypes())
            .eq("shangxia_types", shipin.getShangxiaTypes())
            .eq("shipin_delete", shipin.getShipinDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShipinEntity shipinEntity = shipinService.selectOne(queryWrapper);
        if(shipinEntity==null){
            shipin.setShangxiaTypes(1);
            shipin.setShipinDelete(1);
            shipin.setCreateTime(new Date());
            shipinService.insert(shipin);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ShipinEntity shipin, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,shipin:{}",this.getClass().getName(),shipin.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<ShipinEntity> queryWrapper = new EntityWrapper<ShipinEntity>()
            .notIn("id",shipin.getId())
            .andNew()
            .eq("shipin_uuid_number", shipin.getShipinUuidNumber())
            .eq("shipin_name", shipin.getShipinName())
            .eq("shipin_video", shipin.getShipinVideo())
            .eq("shipin_types", shipin.getShipinTypes())
            .eq("zan_number", shipin.getZanNumber())
            .eq("cai_number", shipin.getCaiNumber())
            .eq("vipshipin_types", shipin.getVipshipinTypes())
            .eq("nianlingduan_types", shipin.getNianlingduanTypes())
            .eq("shangxia_types", shipin.getShangxiaTypes())
            .eq("shipin_delete", shipin.getShipinDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShipinEntity shipinEntity = shipinService.selectOne(queryWrapper);
        if("".equals(shipin.getShipinPhoto()) || "null".equals(shipin.getShipinPhoto())){
                shipin.setShipinPhoto(null);
        }
        if("".equals(shipin.getShipinVideo()) || "null".equals(shipin.getShipinVideo())){
                shipin.setShipinVideo(null);
        }
        if(shipinEntity==null){
            shipinService.updateById(shipin);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        ArrayList<ShipinEntity> list = new ArrayList<>();
        for(Integer id:ids){
            ShipinEntity shipinEntity = new ShipinEntity();
            shipinEntity.setId(id);
            shipinEntity.setShipinDelete(2);
            list.add(shipinEntity);
        }
        if(list != null && list.size() >0){
            shipinService.updateBatchById(list);
        }
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<ShipinEntity> shipinList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("../../upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            ShipinEntity shipinEntity = new ShipinEntity();
//                            shipinEntity.setShipinUuidNumber(data.get(0));                    //视频编号 要改的
//                            shipinEntity.setShipinName(data.get(0));                    //视频名称 要改的
//                            shipinEntity.setShipinPhoto("");//详情和图片
//                            shipinEntity.setShipinVideo(data.get(0));                    //视频试看 要改的
//                            shipinEntity.setShipinTypes(Integer.valueOf(data.get(0)));   //视频类型 要改的
//                            shipinEntity.setZanNumber(Integer.valueOf(data.get(0)));   //赞 要改的
//                            shipinEntity.setCaiNumber(Integer.valueOf(data.get(0)));   //踩 要改的
//                            shipinEntity.setVipshipinTypes(Integer.valueOf(data.get(0)));   //是否vip视频 要改的
//                            shipinEntity.setNianlingduanTypes(Integer.valueOf(data.get(0)));   //适合年龄段 要改的
//                            shipinEntity.setShipinContent("");//详情和图片
//                            shipinEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否上架 要改的
//                            shipinEntity.setShipinDelete(1);//逻辑删除字段
//                            shipinEntity.setCreateTime(date);//时间
                            shipinList.add(shipinEntity);


                            //把要查询是否重复的字段放入map中
                                //视频编号
                                if(seachFields.containsKey("shipinUuidNumber")){
                                    List<String> shipinUuidNumber = seachFields.get("shipinUuidNumber");
                                    shipinUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> shipinUuidNumber = new ArrayList<>();
                                    shipinUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("shipinUuidNumber",shipinUuidNumber);
                                }
                        }

                        //查询是否重复
                         //视频编号
                        List<ShipinEntity> shipinEntities_shipinUuidNumber = shipinService.selectList(new EntityWrapper<ShipinEntity>().in("shipin_uuid_number", seachFields.get("shipinUuidNumber")).eq("shipin_delete", 1));
                        if(shipinEntities_shipinUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(ShipinEntity s:shipinEntities_shipinUuidNumber){
                                repeatFields.add(s.getShipinUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [视频编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        shipinService.insertBatch(shipinList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = shipinService.queryPage(params);

        //字典表数据转换
        List<ShipinView> list =(List<ShipinView>)page.getList();
        for(ShipinView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ShipinEntity shipin = shipinService.selectById(id);
            if(shipin !=null){


                //entity转view
                ShipinView view = new ShipinView();
                BeanUtils.copyProperties( shipin , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody ShipinEntity shipin, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,shipin:{}",this.getClass().getName(),shipin.toString());
        Wrapper<ShipinEntity> queryWrapper = new EntityWrapper<ShipinEntity>()
            .eq("shipin_uuid_number", shipin.getShipinUuidNumber())
            .eq("shipin_name", shipin.getShipinName())
            .eq("shipin_video", shipin.getShipinVideo())
            .eq("shipin_types", shipin.getShipinTypes())
            .eq("zan_number", shipin.getZanNumber())
            .eq("cai_number", shipin.getCaiNumber())
            .eq("vipshipin_types", shipin.getVipshipinTypes())
            .eq("nianlingduan_types", shipin.getNianlingduanTypes())
            .eq("shangxia_types", shipin.getShangxiaTypes())
            .eq("shipin_delete", shipin.getShipinDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShipinEntity shipinEntity = shipinService.selectOne(queryWrapper);
        if(shipinEntity==null){
            shipin.setShipinDelete(1);
            shipin.setCreateTime(new Date());
        shipinService.insert(shipin);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


}
