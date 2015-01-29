drop function if exists oskari_routing_closest_node(geometry);

create or replace function oskari_routing_closest_node(point geometry)
  returns integer as
$BODY$
  declare
    min_node integer;
  begin
    min_node :=
      min(node) from (
        select
          source as node,
          st_distance(point, st_startpoint(geom2d)) as dis
        from hkiroads
        union all
        select
          target as node,
          st_distance(point, st_endpoint(geom2d)) as dis
        from hkiroads) as nodes
        where dis = (select min(dis) from (
          select
            st_distance(point, st_startpoint(geom2d)) as dis
          from hkiroads
          union all
          select
            st_distance(point, st_endpoint(geom2d)) as dis
          from hkiroads) as nodes);

    return min_node;
  end;
$BODY$
  language plpgsql;

