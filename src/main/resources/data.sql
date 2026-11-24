-- [Entrega final] Datos de prueba idempotentes: se insertan por clave natural en cada arranque

-- Se cargan los tipos de emergencia
INSERT INTO dssd.tipo_emergencia (tipo_desastre) SELECT 'Inundación' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_emergencia WHERE tipo_desastre = 'Inundación');
INSERT INTO dssd.tipo_emergencia (tipo_desastre) SELECT 'Incendio forestal' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_emergencia WHERE tipo_desastre = 'Incendio forestal');
INSERT INTO dssd.tipo_emergencia (tipo_desastre) SELECT 'Terremoto' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_emergencia WHERE tipo_desastre = 'Terremoto');
INSERT INTO dssd.tipo_emergencia (tipo_desastre) SELECT 'Maremoto' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_emergencia WHERE tipo_desastre = 'Maremoto');
INSERT INTO dssd.tipo_emergencia (tipo_desastre) SELECT 'Tornado' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_emergencia WHERE tipo_desastre = 'Tornado');
INSERT INTO dssd.tipo_emergencia (tipo_desastre) SELECT 'Deslizamiento de tierra' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_emergencia WHERE tipo_desastre = 'Deslizamiento de tierra');
INSERT INTO dssd.tipo_emergencia (tipo_desastre) SELECT 'Sequía' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_emergencia WHERE tipo_desastre = 'Sequía');

-- Se completa el código estable de los tipos de recurso que ya existían por nombre
UPDATE dssd.tipo_recurso SET codigo = 'ALIMENTOS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Raciones de alimento' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'ALIMENTOS');
UPDATE dssd.tipo_recurso SET codigo = 'AGUA' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Agua potable' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'AGUA');
UPDATE dssd.tipo_recurso SET codigo = 'MEDICAMENTOS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Medicamentos' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'MEDICAMENTOS');
UPDATE dssd.tipo_recurso SET codigo = 'PARAMEDICOS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Paramédicos' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'PARAMEDICOS');
UPDATE dssd.tipo_recurso SET codigo = 'BOMBEROS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Bomberos' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'BOMBEROS');
UPDATE dssd.tipo_recurso SET codigo = 'RESCATISTAS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Rescatistas' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'RESCATISTAS');
UPDATE dssd.tipo_recurso SET codigo = 'FRAZADAS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Frazadas' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'FRAZADAS');
UPDATE dssd.tipo_recurso SET codigo = 'CARPAS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Carpas' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'CARPAS');
UPDATE dssd.tipo_recurso SET codigo = 'GENERADORES' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Generadores eléctricos' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'GENERADORES');
UPDATE dssd.tipo_recurso SET codigo = 'VOLUNTARIOS' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Voluntarios' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'VOLUNTARIOS');
UPDATE dssd.tipo_recurso SET codigo = 'ROPA_ABRIGO' WHERE id = (SELECT MIN(t.id) FROM dssd.tipo_recurso t WHERE t.nombre = 'Ropa de abrigo' AND t.codigo IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso t WHERE t.codigo = 'ROPA_ABRIGO');

-- Se cargan los tipos de recurso del catálogo único (código definido por el Sistema Nacional)
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'ALIMENTOS', 'Raciones de alimento' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'ALIMENTOS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'AGUA', 'Agua potable' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'AGUA');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'MEDICAMENTOS', 'Medicamentos' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'MEDICAMENTOS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'PARAMEDICOS', 'Paramédicos' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'PARAMEDICOS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'BOMBEROS', 'Bomberos' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'BOMBEROS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'RESCATISTAS', 'Rescatistas' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'RESCATISTAS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'FRAZADAS', 'Frazadas' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'FRAZADAS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'CARPAS', 'Carpas' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'CARPAS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'GENERADORES', 'Generadores eléctricos' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'GENERADORES');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'VOLUNTARIOS', 'Voluntarios' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'VOLUNTARIOS');
INSERT INTO dssd.tipo_recurso (codigo, nombre) SELECT 'ROPA_ABRIGO', 'Ropa de abrigo' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_recurso WHERE codigo = 'ROPA_ABRIGO');

-- Se cargan los tipos de organización
INSERT INTO dssd.tipo_organizacion (nombre) SELECT 'ONG' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_organizacion WHERE nombre = 'ONG');
INSERT INTO dssd.tipo_organizacion (nombre) SELECT 'Organismo de rescate' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_organizacion WHERE nombre = 'Organismo de rescate');
INSERT INTO dssd.tipo_organizacion (nombre) SELECT 'Ente gubernamental' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_organizacion WHERE nombre = 'Ente gubernamental');
INSERT INTO dssd.tipo_organizacion (nombre) SELECT 'Organización religiosa' WHERE NOT EXISTS (SELECT 1 FROM dssd.tipo_organizacion WHERE nombre = 'Organización religiosa');

-- Se completa el código de las organizaciones que ya existían por nombre
UPDATE dssd.organizacion SET codigo_ong = 'ONG-CARITAS' WHERE id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.nombre = 'Cáritas' AND o.codigo_ong IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-CARITAS');
UPDATE dssd.organizacion SET codigo_ong = 'ONG-CRUZ-ROJA' WHERE id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.nombre = 'Cruz Roja Argentina' AND o.codigo_ong IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-CRUZ-ROJA');
UPDATE dssd.organizacion SET codigo_ong = 'ONG-BOMBEROS-LP' WHERE id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.nombre = 'Bomberos Voluntarios La Plata' AND o.codigo_ong IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-BOMBEROS-LP');
UPDATE dssd.organizacion SET codigo_ong = 'ONG-MEDICOS-MUNDO' WHERE id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.nombre = 'Médicos del Mundo' AND o.codigo_ong IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-MEDICOS-MUNDO');
UPDATE dssd.organizacion SET codigo_ong = 'ONG-DEFENSA-CIVIL' WHERE id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.nombre = 'Defensa Civil Provincial' AND o.codigo_ong IS NULL) AND NOT EXISTS (SELECT 1 FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-DEFENSA-CIVIL');

-- Se cargan las organizaciones (el codigo_ong identifica a la ONG ante el Sistema Nacional)
INSERT INTO dssd.organizacion (codigo_ong, nombre, tipo, objeto_social, fecha_creacion) SELECT 'ONG-CARITAS', 'Cáritas', 'Organización religiosa', 'Asistencia social y ayuda humanitaria', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.organizacion WHERE codigo_ong = 'ONG-CARITAS');
INSERT INTO dssd.organizacion (codigo_ong, nombre, tipo, objeto_social, fecha_creacion) SELECT 'ONG-CRUZ-ROJA', 'Cruz Roja Argentina', 'ONG', 'Asistencia humanitaria y atención sanitaria en emergencias', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.organizacion WHERE codigo_ong = 'ONG-CRUZ-ROJA');
INSERT INTO dssd.organizacion (codigo_ong, nombre, tipo, objeto_social, fecha_creacion) SELECT 'ONG-BOMBEROS-LP', 'Bomberos Voluntarios La Plata', 'Organismo de rescate', 'Combate de incendios y rescate', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.organizacion WHERE codigo_ong = 'ONG-BOMBEROS-LP');
INSERT INTO dssd.organizacion (codigo_ong, nombre, tipo, objeto_social, fecha_creacion) SELECT 'ONG-MEDICOS-MUNDO', 'Médicos del Mundo', 'ONG', 'Atención médica a poblaciones afectadas', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.organizacion WHERE codigo_ong = 'ONG-MEDICOS-MUNDO');
INSERT INTO dssd.organizacion (codigo_ong, nombre, tipo, objeto_social, fecha_creacion) SELECT 'ONG-DEFENSA-CIVIL', 'Defensa Civil Provincial', 'Ente gubernamental', 'Prevención y respuesta ante desastres', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.organizacion WHERE codigo_ong = 'ONG-DEFENSA-CIVIL');

-- [Entrega final] Se renombran los usuarios de prueba previos a los nombres actuales de Bonita
UPDATE dssd.usuario SET usuario_bonita = 'olmos_municipio' WHERE usuario_bonita = 'olmos' AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio');
UPDATE dssd.usuario SET usuario_bonita = 'caritas_ong' WHERE usuario_bonita = 'caritas' AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'caritas_ong');
UPDATE dssd.usuario SET usuario_bonita = 'cruz_roja_ong' WHERE usuario_bonita = 'cruz_roja' AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'cruz_roja_ong');

-- Se cargan los usuarios de prueba vinculados a Bonita (CUIL ficticios)
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Olga', 'Olmos', '20000000011', 'OPERADOR_MUNICIPAL', 'olmos_municipio', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'olmos_municipio' OR cuil = '20000000011');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Laura', 'Plata', '20000000061', 'OPERADOR_MUNICIPAL', 'la_plata_municipio', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'la_plata_municipio' OR cuil = '20000000061');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Ernesto', 'Ensenada', '20000000079', 'OPERADOR_MUNICIPAL', 'ensenada_municipio', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'ensenada_municipio' OR cuil = '20000000079');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Carlos', 'Coordinador', '20000000029', 'COORDINADOR', 'agente_coordinador_1', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'agente_coordinador_1' OR cuil = '20000000029');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Rosa', 'Representante', '20000000037', 'ONG', 'caritas_ong', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'caritas_ong' OR cuil = '20000000037');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Raúl', 'Rescate', '20000000053', 'ONG', 'cruz_roja_ong', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'cruz_roja_ong' OR cuil = '20000000053');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Bruno', 'Bombero', '20000000087', 'ONG', 'bomberos_lp_ong', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'bomberos_lp_ong' OR cuil = '20000000087');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Marta', 'Médica', '20000000095', 'ONG', 'medicos_mundo_ong', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'medicos_mundo_ong' OR cuil = '20000000095');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Diego', 'Defensa', '20000000109', 'ONG', 'defensa_civil_ong', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'defensa_civil_ong' OR cuil = '20000000109');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Adrián', 'Auditor', '20000000045', 'AUDITOR', 'auditor', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'auditor' OR cuil = '20000000045');

-- Se vinculan con Bonita los usuarios de prueba que ya existían sin usuario_bonita
UPDATE dssd.usuario SET usuario_bonita = 'olmos_municipio' WHERE cuil = '20000000011' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio');
UPDATE dssd.usuario SET usuario_bonita = 'la_plata_municipio' WHERE cuil = '20000000061' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio');
UPDATE dssd.usuario SET usuario_bonita = 'ensenada_municipio' WHERE cuil = '20000000079' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'ensenada_municipio');
UPDATE dssd.usuario SET usuario_bonita = 'agente_coordinador_1' WHERE cuil = '20000000029' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'agente_coordinador_1');
UPDATE dssd.usuario SET usuario_bonita = 'caritas_ong' WHERE cuil = '20000000037' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'caritas_ong');
UPDATE dssd.usuario SET usuario_bonita = 'cruz_roja_ong' WHERE cuil = '20000000053' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'cruz_roja_ong');
UPDATE dssd.usuario SET usuario_bonita = 'bomberos_lp_ong' WHERE cuil = '20000000087' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'bomberos_lp_ong');
UPDATE dssd.usuario SET usuario_bonita = 'medicos_mundo_ong' WHERE cuil = '20000000095' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'medicos_mundo_ong');
UPDATE dssd.usuario SET usuario_bonita = 'defensa_civil_ong' WHERE cuil = '20000000109' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'defensa_civil_ong');
UPDATE dssd.usuario SET usuario_bonita = 'auditor' WHERE cuil = '20000000045' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'auditor');

-- Se normalizan los roles locales de los usuarios de prueba (respaldo si Bonita no expone las membresías)
UPDATE dssd.usuario SET rol = 'OPERADOR_MUNICIPAL' WHERE usuario_bonita IN ('olmos_municipio', 'la_plata_municipio', 'ensenada_municipio');
UPDATE dssd.usuario SET rol = 'COORDINADOR' WHERE usuario_bonita IN ('agente_coordinador_1');
UPDATE dssd.usuario SET rol = 'ONG' WHERE usuario_bonita IN ('caritas_ong', 'cruz_roja_ong', 'bomberos_lp_ong', 'medicos_mundo_ong', 'defensa_civil_ong');
UPDATE dssd.usuario SET rol = 'AUDITOR' WHERE usuario_bonita IN ('auditor');

-- Se vinculan los representantes con su organización
UPDATE dssd.usuario SET organizacion_id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-CARITAS') WHERE usuario_bonita = 'caritas_ong' AND organizacion_id IS NULL;
UPDATE dssd.usuario SET organizacion_id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-CRUZ-ROJA') WHERE usuario_bonita = 'cruz_roja_ong' AND organizacion_id IS NULL;
UPDATE dssd.usuario SET organizacion_id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-BOMBEROS-LP') WHERE usuario_bonita = 'bomberos_lp_ong' AND organizacion_id IS NULL;
UPDATE dssd.usuario SET organizacion_id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-MEDICOS-MUNDO') WHERE usuario_bonita = 'medicos_mundo_ong' AND organizacion_id IS NULL;
UPDATE dssd.usuario SET organizacion_id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-DEFENSA-CIVIL') WHERE usuario_bonita = 'defensa_civil_ong' AND organizacion_id IS NULL;

-- Se quitan los CHECK de enums que haya generado Hibernate (bloquean los estados nuevos)
ALTER TABLE dssd.oferta DROP CONSTRAINT IF EXISTS oferta_estado_check;
ALTER TABLE dssd.oferta_version DROP CONSTRAINT IF EXISTS oferta_version_estado_check;
ALTER TABLE dssd.notificacion DROP CONSTRAINT IF EXISTS notificacion_tipo_check;

-- Se normalizan los estados de ofertas previas (antes eran texto libre)
UPDATE dssd.oferta SET estado = 'PENDIENTE' WHERE estado IS NULL OR estado NOT IN ('PENDIENTE', 'VALIDADA', 'NO_HABILITADA', 'SELECCIONADA', 'NO_SELECCIONADA', 'ADJUDICADA', 'FINALIZADA', 'RETIRADA');

-- Se completan la versión y la fecha de modificación de los lotes previos
UPDATE dssd.oferta SET numero_version = 1 WHERE numero_version IS NULL;
UPDATE dssd.oferta SET fecha_modificacion = CAST(fecha_creacion AS timestamp) WHERE fecha_modificacion IS NULL AND fecha_creacion IS NOT NULL;

-- Se completan la ronda y la cobertura parcial de las convocatorias previas
UPDATE dssd.convocatoria SET ronda = 1 WHERE ronda IS NULL;
UPDATE dssd.convocatoria SET cobertura_parcial_aceptada = false WHERE cobertura_parcial_aceptada IS NULL;

-- Se cargan las especialidades de las ONGs de prueba
INSERT INTO dssd.organizacion_especialidad (organizacion_id, tipo_recurso_id) SELECT o.id, t.id FROM dssd.organizacion o JOIN dssd.tipo_recurso t ON t.codigo IN ('ALIMENTOS', 'AGUA', 'FRAZADAS', 'ROPA_ABRIGO', 'CARPAS') WHERE o.codigo_ong = 'ONG-CARITAS' AND NOT EXISTS (SELECT 1 FROM dssd.organizacion_especialidad e WHERE e.organizacion_id = o.id AND e.tipo_recurso_id = t.id);
INSERT INTO dssd.organizacion_especialidad (organizacion_id, tipo_recurso_id) SELECT o.id, t.id FROM dssd.organizacion o JOIN dssd.tipo_recurso t ON t.codigo IN ('PARAMEDICOS', 'MEDICAMENTOS', 'AGUA', 'ALIMENTOS', 'VOLUNTARIOS') WHERE o.codigo_ong = 'ONG-CRUZ-ROJA' AND NOT EXISTS (SELECT 1 FROM dssd.organizacion_especialidad e WHERE e.organizacion_id = o.id AND e.tipo_recurso_id = t.id);
INSERT INTO dssd.organizacion_especialidad (organizacion_id, tipo_recurso_id) SELECT o.id, t.id FROM dssd.organizacion o JOIN dssd.tipo_recurso t ON t.codigo IN ('BOMBEROS', 'RESCATISTAS', 'GENERADORES') WHERE o.codigo_ong = 'ONG-BOMBEROS-LP' AND NOT EXISTS (SELECT 1 FROM dssd.organizacion_especialidad e WHERE e.organizacion_id = o.id AND e.tipo_recurso_id = t.id);
INSERT INTO dssd.organizacion_especialidad (organizacion_id, tipo_recurso_id) SELECT o.id, t.id FROM dssd.organizacion o JOIN dssd.tipo_recurso t ON t.codigo IN ('MEDICAMENTOS', 'PARAMEDICOS') WHERE o.codigo_ong = 'ONG-MEDICOS-MUNDO' AND NOT EXISTS (SELECT 1 FROM dssd.organizacion_especialidad e WHERE e.organizacion_id = o.id AND e.tipo_recurso_id = t.id);
INSERT INTO dssd.organizacion_especialidad (organizacion_id, tipo_recurso_id) SELECT o.id, t.id FROM dssd.organizacion o JOIN dssd.tipo_recurso t ON t.codigo IN ('RESCATISTAS', 'CARPAS', 'GENERADORES', 'VOLUNTARIOS') WHERE o.codigo_ong = 'ONG-DEFENSA-CIVIL' AND NOT EXISTS (SELECT 1 FROM dssd.organizacion_especialidad e WHERE e.organizacion_id = o.id AND e.tipo_recurso_id = t.id);

-- Se carga un inventario inicial para las cinco ONGs de prueba
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 500, 'Depósito central', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CARITAS' AND t.codigo = 'ALIMENTOS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 300, 'Donaciones de invierno', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CARITAS' AND t.codigo = 'FRAZADAS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 1500, 'Reserva de bidones 5 L', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CARITAS' AND t.codigo = 'AGUA' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 40, 'Equipos de guardia', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CRUZ-ROJA' AND t.codigo = 'PARAMEDICOS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 2000, 'Bidones de 5 L', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CRUZ-ROJA' AND t.codigo = 'AGUA' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 60, 'Dotación activa', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-BOMBEROS-LP' AND t.codigo = 'BOMBEROS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 45, 'Brigada de rescate urbano', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-BOMBEROS-LP' AND t.codigo = 'RESCATISTAS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 600, 'Botiquines de trauma y crónicos', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-MEDICOS-MUNDO' AND t.codigo = 'MEDICAMENTOS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 120, 'Carpas familiares impermeables', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-DEFENSA-CIVIL' AND t.codigo = 'CARPAS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 25, 'Grupos electrógenos portátiles', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-DEFENSA-CIVIL' AND t.codigo = 'GENERADORES' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);

-- Se asignan al operador municipal las emergencias previas sin autor
UPDATE dssd.emergencia SET creado_por = (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio') WHERE creado_por IS NULL;

-- ============================================================================
-- [Entrega final] Dataset histórico idempotente para el tablero del auditor
-- 20 emergencias distribuidas en 6 meses, cubriendo las 8 etapas, 4 gravedades,
-- 6 tipos de desastre, 11 tipos de recurso, 5 ONGs, consorcios, reaperturas,
-- cobertura parcial y proyectos en curso y finalizados. (case_id = NULL)
-- ============================================================================

-- 1. Proyectos (13 TERMINADO con duraciones entre 24h y 120h + 2 EN_CURSO)
INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Inundación Tolosa', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '150 days', LOCALTIMESTAMP - INTERVAL '148 days'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Inundación Tolosa');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Incendio Pereyra Sur', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '138 days', LOCALTIMESTAMP - INTERVAL '136 days' - INTERVAL '12 hours'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Incendio Pereyra Sur');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Cuenca Arroyo El Gato', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '119 days', LOCALTIMESTAMP - INTERVAL '115 days'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Cuenca Arroyo El Gato');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Tornado Punta Lara', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '112 days', LOCALTIMESTAMP - INTERVAL '109 days' - INTERVAL '12 hours'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Tornado Punta Lara');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Sequía Zona Rural Olmos', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '103 days', LOCALTIMESTAMP - INTERVAL '100 days'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Sequía Zona Rural Olmos');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Sudestada Berisso-Ensenada', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '90 days', LOCALTIMESTAMP - INTERVAL '86 days' - INTERVAL '12 hours'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Sudestada Berisso-Ensenada');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Deslizamiento Ruta 36 Abasto', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '80 days', LOCALTIMESTAMP - INTERVAL '77 days' - INTERVAL '18 hours'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Deslizamiento Ruta 36 Abasto');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Foco Forestal Villa Elisa', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '72 days', LOCALTIMESTAMP - INTERVAL '71 days'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Foco Forestal Villa Elisa');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Sismo Zona Sur Gran La Plata', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '60 days', LOCALTIMESTAMP - INTERVAL '55 days'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Sismo Zona Sur Gran La Plata');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Temporal Melchor Romero', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '50 days', LOCALTIMESTAMP - INTERVAL '48 days'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Temporal Melchor Romero');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Déficit Hídrico Etcheverry', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '42 days', LOCALTIMESTAMP - INTERVAL '40 days' - INTERVAL '8 hours'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Déficit Hídrico Etcheverry');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Inundación Ringuelet y Gonnet', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '30 days', LOCALTIMESTAMP - INTERVAL '27 days' - INTERVAL '8 hours'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Inundación Ringuelet y Gonnet');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Incendio Isla Santiago', 'TERMINADO', LOCALTIMESTAMP - INTERVAL '22 days', LOCALTIMESTAMP - INTERVAL '19 days' - INTERVAL '20 hours'
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Incendio Isla Santiago');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Cuenca Maldonado Los Hornos', 'EN_CURSO', LOCALTIMESTAMP - INTERVAL '14 days', NULL
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Cuenca Maldonado Los Hornos');

INSERT INTO dssd.proyecto (nombre, estado, fecha_inicio, fecha_fin)
SELECT 'Operativo Tornado City Bell', 'EN_CURSO', LOCALTIMESTAMP - INTERVAL '8 days', NULL
WHERE NOT EXISTS (SELECT 1 FROM dssd.proyecto WHERE nombre = 'Operativo Tornado City Bell');

-- 2. Emergencias históricas (20 emergencias en 6 meses)
INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Inundación', 'Alta', 'La Plata - Tolosa', CURRENT_DATE - 152, 'Desborde pluvial en sector norte de Tolosa con evacuados.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Inundación Tolosa'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Incendio forestal', 'Media', 'Parque Pereyra Iraola Sur', CURRENT_DATE - 140, 'Foco ígneo sobre pastizales y arboleda linderos al camino Centenario.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Incendio Pereyra Sur'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'ensenada_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Foco ígneo sobre pastizales y arboleda linderos al camino Centenario.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Inundación', 'Crítica', 'La Plata - Arroyo El Gato', CURRENT_DATE - 122, 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Cuenca Arroyo El Gato'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Tornado', 'Alta', 'Ensenada - Punta Lara', CURRENT_DATE - 114, 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Tornado Punta Lara'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'ensenada_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Sequía', 'Media', 'Lisandro Olmos - Zona Rural', CURRENT_DATE - 105, 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Sequía Zona Rural Olmos'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Inundación', 'Crítica', 'Ensenada - Zona Ribereña', CURRENT_DATE - 92, 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Sudestada Berisso-Ensenada'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'ensenada_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Deslizamiento de tierra', 'Alta', 'Abasto - Ruta 36', CURRENT_DATE - 82, 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Deslizamiento Ruta 36 Abasto'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Incendio forestal', 'Baja', 'La Plata - Villa Elisa', CURRENT_DATE - 74, 'Quema descontrolada de rastrojos contenida en perímetro rural de Villa Elisa.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Foco Forestal Villa Elisa'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Quema descontrolada de rastrojos contenida en perímetro rural de Villa Elisa.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Terremoto', 'Crítica', 'Gran La Plata - Sector Sur', CURRENT_DATE - 62, 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Sismo Zona Sur Gran La Plata'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Tornado', 'Alta', 'Melchor Romero', CURRENT_DATE - 52, 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Temporal Melchor Romero'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Sequía', 'Baja', 'Ángel Etcheverry', CURRENT_DATE - 44, 'Abastecimiento preventivo de agua en escuelas rurales y puestos sanitarios.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Déficit Hídrico Etcheverry'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Abastecimiento preventivo de agua en escuelas rurales y puestos sanitarios.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Inundación', 'Alta', 'La Plata - Ringuelet y Gonnet', CURRENT_DATE - 32, 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Inundación Ringuelet y Gonnet'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Incendio forestal', 'Alta', 'Ensenada - Isla Santiago', CURRENT_DATE - 24, 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Incendio Isla Santiago'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'ensenada_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Inundación', 'Crítica', 'La Plata - Los Hornos', CURRENT_DATE - 16, 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Cuenca Maldonado Los Hornos'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Tornado', 'Alta', 'La Plata - City Bell', CURRENT_DATE - 10, 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.', NULL,
       (SELECT MIN(p.id) FROM dssd.proyecto p WHERE p.nombre = 'Operativo Tornado City Bell'),
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Inundación', 'Alta', 'Lisandro Olmos Centro', CURRENT_DATE - 7, 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.', NULL, NULL,
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Deslizamiento de tierra', 'Media', 'Ensenada - Camino Rivadavia', CURRENT_DATE - 6, 'Socavamiento de talud lindero al canal con cobertura de ofertas insuficiente.', NULL, NULL,
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'ensenada_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Socavamiento de talud lindero al canal con cobertura de ofertas insuficiente.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Incendio forestal', 'Media', 'Parque Pereyra Sector Norte', CURRENT_DATE - 4, 'Incendio de malezas en borde norte del parque con ofertas en proceso de validación.', NULL, NULL,
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Incendio de malezas en borde norte del parque con ofertas en proceso de validación.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Sequía', 'Media', 'Abasto y Etcheverry', CURRENT_DATE - 2, 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.', NULL, NULL,
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.');

INSERT INTO dssd.emergencia (tipo_desastre, nivel_gravedad, zona_afectada, fecha_creacion, descripcion, case_id, proyecto_id, creado_por)
SELECT 'Tornado', 'Alta', 'La Plata - Estación Tolosa', CURRENT_DATE - 1, 'Alerta reciente por ráfagas intensas en zona de estación Tolosa en relevamiento inicial.', NULL, NULL,
       (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'la_plata_municipio')
WHERE NOT EXISTS (SELECT 1 FROM dssd.emergencia WHERE descripcion = 'Alerta reciente por ráfagas intensas en zona de estación Tolosa en relevamiento inicial.');

-- 3. Convocatorias (19 publicadas, la emergencia 20 queda EN_RELEVAMIENTO sin convocatoria)
INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 152, LOCALTIMESTAMP - INTERVAL '152 days', LOCALTIMESTAMP - INTERVAL '151 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 140, LOCALTIMESTAMP - INTERVAL '140 days', LOCALTIMESTAMP - INTERVAL '139 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Foco ígneo sobre pastizales y arboleda linderos al camino Centenario.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 122, LOCALTIMESTAMP - INTERVAL '121 days', LOCALTIMESTAMP - INTERVAL '120 days', e.id, 2, false
FROM dssd.emergencia e WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 114, LOCALTIMESTAMP - INTERVAL '114 days', LOCALTIMESTAMP - INTERVAL '113 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 105, LOCALTIMESTAMP - INTERVAL '105 days', LOCALTIMESTAMP - INTERVAL '104 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 92, LOCALTIMESTAMP - INTERVAL '92 days', LOCALTIMESTAMP - INTERVAL '91 days', e.id, 1, true
FROM dssd.emergencia e WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 82, LOCALTIMESTAMP - INTERVAL '82 days', LOCALTIMESTAMP - INTERVAL '81 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 74, LOCALTIMESTAMP - INTERVAL '74 days', LOCALTIMESTAMP - INTERVAL '73 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Quema descontrolada de rastrojos contenida en perímetro rural de Villa Elisa.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 62, LOCALTIMESTAMP - INTERVAL '62 days', LOCALTIMESTAMP - INTERVAL '61 days', e.id, 2, false
FROM dssd.emergencia e WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 52, LOCALTIMESTAMP - INTERVAL '52 days', LOCALTIMESTAMP - INTERVAL '51 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 44, LOCALTIMESTAMP - INTERVAL '44 days', LOCALTIMESTAMP - INTERVAL '43 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Abastecimiento preventivo de agua en escuelas rurales y puestos sanitarios.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 32, LOCALTIMESTAMP - INTERVAL '32 days', LOCALTIMESTAMP - INTERVAL '31 days', e.id, 1, true
FROM dssd.emergencia e WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 24, LOCALTIMESTAMP - INTERVAL '24 days', LOCALTIMESTAMP - INTERVAL '23 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 16, LOCALTIMESTAMP - INTERVAL '16 days', LOCALTIMESTAMP - INTERVAL '15 days', e.id, 2, false
FROM dssd.emergencia e WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 10, LOCALTIMESTAMP - INTERVAL '10 days', LOCALTIMESTAMP - INTERVAL '9 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 7, LOCALTIMESTAMP - INTERVAL '7 days', LOCALTIMESTAMP - INTERVAL '2 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 6, LOCALTIMESTAMP - INTERVAL '6 days', LOCALTIMESTAMP - INTERVAL '2 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Socavamiento de talud lindero al canal con cobertura de ofertas insuficiente.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 4, LOCALTIMESTAMP - INTERVAL '4 days', LOCALTIMESTAMP - INTERVAL '1 day', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Incendio de malezas en borde norte del parque con ofertas en proceso de validación.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

INSERT INTO dssd.convocatoria (estado, fecha_creacion, fecha_apertura, fecha_cierre, emergencia_id, ronda, cobertura_parcial_aceptada)
SELECT 'PUBLICADA', CURRENT_DATE - 2, LOCALTIMESTAMP - INTERVAL '2 days', LOCALTIMESTAMP + INTERVAL '7 days', e.id, 1, false
FROM dssd.emergencia e WHERE e.descripcion = 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.'
AND NOT EXISTS (SELECT 1 FROM dssd.convocatoria c WHERE c.emergencia_id = e.id);

-- 4. Necesidades (lotes requeridos por convocatoria)
INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Bidones de agua potable para evacuados', 600, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Viandas y alimentos secos', 350, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Rescatistas en zonas anegadas', 25, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Dotaciones de bomberos forestales', 30, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Foco ígneo sobre pastizales y arboleda linderos al camino Centenario.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Insumos médicos para quemaduras e inhalación', 120, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Foco ígneo sobre pastizales y arboleda linderos al camino Centenario.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Agua embotellada para centros de evacuación', 1000, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Raciones diarias para familias evacuadas', 600, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Carpas de alojamiento de emergencia', 80, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS'
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Frazadas térmicas', 400, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS'
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Carpas para familias con techos dañados', 60, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS'
WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Generadores para postas y bombas', 12, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES'
WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Frazadas para asistencia nocturna', 300, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS'
WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Agua potable en bidones de 5 L', 2000, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Módulos alimentarios básicos', 250, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Equipos de paramédicos en ribera', 40, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Medicamentos respiratorios y antibióticos', 350, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Kits de ropa de abrigo seca', 500, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ROPA_ABRIGO'
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Brigadas de rescate y estabilización', 40, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Generadores para iluminación nocturna', 15, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES'
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Voluntarios para logística y contención', 60, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'VOLUNTARIOS'
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Dotaciones de bomberos voluntarios', 18, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Quema descontrolada de rastrojos contenida en perímetro rural de Villa Elisa.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Hidratación para brigadistas', 300, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Quema descontrolada de rastrojos contenida en perímetro rural de Villa Elisa.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Equipos de rescate en estructuras colapsadas', 55, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Carpas estructurales para campamento', 110, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Medicamentos de emergencia y trauma', 500, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Paramédicos de atención prehospitalaria', 45, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Raciones de alimento para campamento', 700, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Agua potable envasada', 1100, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Indumentaria de abrigo post-temporal', 380, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ROPA_ABRIGO'
WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Frazadas para familias afectadas', 320, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS'
WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Generadores para centro comunitario', 10, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES'
WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Bidones de agua para escuelas rurales', 750, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Abastecimiento preventivo de agua en escuelas rurales y puestos sanitarios.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Voluntarios para distribución puerta a puerta', 30, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'VOLUNTARIOS'
WHERE e.descripcion = 'Abastecimiento preventivo de agua en escuelas rurales y puestos sanitarios.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Raciones de alimento caliente y seco', 600, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Agua potable en bidones', 1000, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Kits de medicamentos esenciales', 250, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Dotaciones de bomberos con autobombas', 40, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Rescatistas ribereños', 20, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Paramédicos en posta móvil', 20, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Medicamentos broncodilatadores y gasas', 180, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Agua potable en bidones familiares', 850, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Carpas de resguardo temporal', 70, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Raciones alimentarias para evacuados', 500, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Paramédicos para centros de evacuación', 25, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Generadores eléctricos de respaldo', 18, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES'
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Frazadas para hogares sin energía', 400, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS'
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Medicamentos para atención de heridos leves', 150, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Agua potable para centro urbano', 800, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Raciones de alimento', 350, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Botiquines y medicamentos básicos', 200, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Rescatistas especializados en taludes', 40, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Socavamiento de talud lindero al canal con cobertura de ofertas insuficiente.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Carpas de relocalización preventiva', 50, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS'
WHERE e.descripcion = 'Socavamiento de talud lindero al canal con cobertura de ofertas insuficiente.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Bomberos de ataque inicial', 25, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Incendio de malezas en borde norte del parque con ofertas en proceso de validación.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Paramédicos de apoyo', 15, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Incendio de malezas en borde norte del parque con ofertas en proceso de validación.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Agua potable para productores y familias', 1200, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

INSERT INTO dssd.necesidad (fecha_creacion, descripcion, cantidad, convocatoria_id, tipo_recurso_id)
SELECT c.fecha_creacion, 'Voluntarios de distribución', 40, c.id, t.id
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.tipo_recurso t ON t.codigo = 'VOLUNTARIOS'
WHERE e.descripcion = 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.'
AND NOT EXISTS (SELECT 1 FROM dssd.necesidad n WHERE n.convocatoria_id = c.id AND n.tipo_recurso_id = t.id);

-- 5. Ofertas conjuntas (7 consorcios válidos distribuidos en los 6 meses)
INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '3 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS'
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong = 'ONG-CRUZ-ROJA'
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '4 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS'
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong IN ('ONG-CRUZ-ROJA', 'ONG-DEFENSA-CIVIL')
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '3 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA'
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong = 'ONG-MEDICOS-MUNDO'
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '2 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP'
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong = 'ONG-DEFENSA-CIVIL'
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '4 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong IN ('ONG-BOMBEROS-LP', 'ONG-MEDICOS-MUNDO')
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '3 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS'
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong = 'ONG-CRUZ-ROJA'
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '3 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong = 'ONG-DEFENSA-CIVIL'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

INSERT INTO dssd.oferta_conjunta (convocatoria_id, organizacion_duena_id, fecha_creacion)
SELECT c.id, o.id, c.fecha_apertura + INTERVAL '2 hours'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL'
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta oc WHERE oc.convocatoria_id = c.id AND oc.organizacion_duena_id = o.id);

INSERT INTO dssd.oferta_conjunta_participante (oferta_conjunta_id, organizacion_id)
SELECT oc.id, p.id
FROM dssd.oferta_conjunta oc JOIN dssd.convocatoria c ON c.id = oc.convocatoria_id JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion p ON p.codigo_ong = 'ONG-CARITAS'
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta_conjunta_participante ocp WHERE ocp.oferta_conjunta_id = oc.id AND ocp.organizacion_id = p.id);

-- 6. Ofertas (50+ lotes cubriendo todos los estados y organizaciones)

-- E01: Tolosa (FINALIZADA - Consorcio Cáritas+Cruz Roja + Bomberos + NO_SELECCIONADA Defensa Civil)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 350, 'Alimentos secos en consorcio Tolosa', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '148 days', LOCALTIMESTAMP - INTERVAL '150 days', LOCALTIMESTAMP - INTERVAL '148 days', 'Entrega completa en centro comunitario de Tolosa.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Alimentos secos en consorcio Tolosa');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 600, 'Bidones de agua potable en consorcio Tolosa', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '148 days', LOCALTIMESTAMP - INTERVAL '150 days', LOCALTIMESTAMP - INTERVAL '148 days', 'Distribución verificada por municipio.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bidones de agua potable en consorcio Tolosa');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 25, 'Rescatistas con botes en Tolosa', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '148 days', LOCALTIMESTAMP - INTERVAL '150 days', LOCALTIMESTAMP - INTERVAL '148 days', 'Evacuación preventiva completada sin incidentes.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Rescatistas con botes en Tolosa');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 15, 'Rescatistas alternativos Defensa Civil Tolosa', 'NO_SELECCIONADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 3, LOCALTIMESTAMP - INTERVAL '150 days', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Desborde pluvial en sector norte de Tolosa con evacuados.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Rescatistas alternativos Defensa Civil Tolosa');

-- E02: Incendio Pereyra Sur (FINALIZADA - individuales)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 30, 'Bomberos forestales en Pereyra Sur', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '136 days', LOCALTIMESTAMP - INTERVAL '138 days', LOCALTIMESTAMP - INTERVAL '136 days' - INTERVAL '12 hours', 'Foco extinguido y guardia de cenizas cerrada.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Foco ígneo sobre pastizales y arboleda linderos al camino Centenario.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bomberos forestales en Pereyra Sur');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 120, 'Kits médicos Pereyra Sur', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '136 days', LOCALTIMESTAMP - INTERVAL '138 days', LOCALTIMESTAMP - INTERVAL '136 days' - INTERVAL '12 hours', 'Insumos entregados al puesto sanitario.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Foco ígneo sobre pastizales y arboleda linderos al camino Centenario.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Kits médicos Pereyra Sur');

-- E03: Arroyo El Gato (FINALIZADA - Consorcio Cáritas + Cruz Roja + Defensa Civil, ronda 2)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 600, 'Raciones alimentarias consorcio El Gato', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 2, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '115 days', LOCALTIMESTAMP - INTERVAL '119 days', LOCALTIMESTAMP - INTERVAL '115 days', 'Entregadas en 4 centros de evacuación.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Raciones alimentarias consorcio El Gato');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 400, 'Frazadas térmicas consorcio El Gato', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 2, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '115 days', LOCALTIMESTAMP - INTERVAL '119 days', LOCALTIMESTAMP - INTERVAL '115 days', 'Frazadas distribuidas según padrón.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Frazadas térmicas consorcio El Gato');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 1000, 'Agua embotellada consorcio El Gato', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 2, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '115 days', LOCALTIMESTAMP - INTERVAL '119 days', LOCALTIMESTAMP - INTERVAL '115 days', 'Provisión completa sin faltantes.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua embotellada consorcio El Gato');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 80, 'Carpas de emergencia consorcio El Gato', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 2, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '115 days', LOCALTIMESTAMP - INTERVAL '119 days', LOCALTIMESTAMP - INTERVAL '115 days', 'Carpas instaladas y desmontadas al finalizar.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Crecida severa del Arroyo El Gato con corte de accesos y centros de evacuación activos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Carpas de emergencia consorcio El Gato');

-- E04: Tornado Punta Lara (FINALIZADA - individuales + 1 NO_HABILITADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 60, 'Carpas impermeables Punta Lara', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '109 days', LOCALTIMESTAMP - INTERVAL '112 days', LOCALTIMESTAMP - INTERVAL '109 days' - INTERVAL '12 hours', 'Despliegue finalizado en barrios ribereños.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS'
WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Carpas impermeables Punta Lara');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 12, 'Generadores portátiles Punta Lara', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '109 days', LOCALTIMESTAMP - INTERVAL '112 days', LOCALTIMESTAMP - INTERVAL '109 days' - INTERVAL '12 hours', 'Restablecida red eléctrica, generadores retirados.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES'
WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Generadores portátiles Punta Lara');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 300, 'Frazadas Cáritas Punta Lara', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '109 days', LOCALTIMESTAMP - INTERVAL '112 days', LOCALTIMESTAMP - INTERVAL '109 days' - INTERVAL '12 hours', 'Entrega documentada en delegación Ensenada.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS'
WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Frazadas Cáritas Punta Lara');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 10, 'Generadores fuera de certificación Punta Lara', 'NO_HABILITADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '113 days', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES'
WHERE e.descripcion = 'Ráfagas severas con voladura de techos y corte prolongado de suministro eléctrico en Punta Lara.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Generadores fuera de certificación Punta Lara');

-- E05: Sequía Olmos (FINALIZADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 1200, 'Bidones de agua Cruz Roja Olmos', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '100 days', LOCALTIMESTAMP - INTERVAL '103 days', LOCALTIMESTAMP - INTERVAL '100 days', 'Distribución completada en zona de quintas.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bidones de agua Cruz Roja Olmos');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 800, 'Bidones de agua Cáritas Olmos', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '100 days', LOCALTIMESTAMP - INTERVAL '103 days', LOCALTIMESTAMP - INTERVAL '100 days', 'Entrega realizada junto a delegación municipal.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bidones de agua Cáritas Olmos');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 250, 'Módulos alimentarios Cáritas Olmos', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '100 days', LOCALTIMESTAMP - INTERVAL '103 days', LOCALTIMESTAMP - INTERVAL '100 days', 'Módulos entregados al 100%.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Escasez crítica de agua potable en quintas y barrios rurales de Lisandro Olmos.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Módulos alimentarios Cáritas Olmos');

-- E06: Sudestada Ensenada (FINALIZADA - cobertura parcial aceptada + Consorcio Cruz Roja+Médicos del Mundo)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 35, 'Paramédicos consorcio sanitario Ribera', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '86 days', LOCALTIMESTAMP - INTERVAL '90 days', LOCALTIMESTAMP - INTERVAL '86 days' - INTERVAL '12 hours', 'Guardias sanitarias completadas en costa.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Paramédicos consorcio sanitario Ribera');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 300, 'Medicamentos consorcio sanitario Ribera', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '86 days', LOCALTIMESTAMP - INTERVAL '90 days', LOCALTIMESTAMP - INTERVAL '86 days' - INTERVAL '12 hours', 'Stock aplicado en postas de evacuados.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Medicamentos consorcio sanitario Ribera');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 450, 'Ropa de abrigo Cáritas Ribera', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '86 days', LOCALTIMESTAMP - INTERVAL '90 days', LOCALTIMESTAMP - INTERVAL '86 days' - INTERVAL '12 hours', 'Kits de abrigo entregados a todas las familias.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'ROPA_ABRIGO'
WHERE e.descripcion = 'Sudestada intensa con anegamiento costero y evacuación preventiva de familias.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Ropa de abrigo Cáritas Ribera');

-- E07: Deslizamiento Abasto (FINALIZADA - Consorcio Bomberos+Defensa Civil)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 40, 'Rescatistas consorcio Abasto', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '77 days', LOCALTIMESTAMP - INTERVAL '80 days', LOCALTIMESTAMP - INTERVAL '77 days' - INTERVAL '18 hours', 'Zona asegurada y familias reubicadas.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Rescatistas consorcio Abasto');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 15, 'Generadores consorcio Abasto', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '77 days', LOCALTIMESTAMP - INTERVAL '80 days', LOCALTIMESTAMP - INTERVAL '77 days' - INTERVAL '18 hours', 'Iluminación operativa durante 54 horas.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Generadores consorcio Abasto');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 60, 'Voluntarios consorcio Abasto', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '77 days', LOCALTIMESTAMP - INTERVAL '80 days', LOCALTIMESTAMP - INTERVAL '77 days' - INTERVAL '18 hours', 'Asistencia logística concluida.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'VOLUNTARIOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Desmoronamiento de barrancas tras lluvias acumuladas con viviendas comprometidas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Voluntarios consorcio Abasto');

-- E08: Villa Elisa (FINALIZADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 18, 'Bomberos ataque rápido Villa Elisa', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '71 days', LOCALTIMESTAMP - INTERVAL '72 days', LOCALTIMESTAMP - INTERVAL '71 days', 'Perímetro enfriado completamente.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Quema descontrolada de rastrojos contenida en perímetro rural de Villa Elisa.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bomberos ataque rápido Villa Elisa');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 300, 'Agua para brigadistas Villa Elisa', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '71 days', LOCALTIMESTAMP - INTERVAL '72 days', LOCALTIMESTAMP - INTERVAL '71 days', 'Hidratación cubierta en toda la jornada.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Quema descontrolada de rastrojos contenida en perímetro rural de Villa Elisa.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua para brigadistas Villa Elisa');

-- E09: Terremoto Gran La Plata (FINALIZADA - Consorcio Defensa Civil + Bomberos + Médicos del Mundo, ronda 2)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 110, 'Carpas campamento consorcio Sismo', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '55 days', LOCALTIMESTAMP - INTERVAL '60 days', LOCALTIMESTAMP - INTERVAL '55 days', 'Campamento operativo durante 5 días.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Carpas campamento consorcio Sismo');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 55, 'Rescatistas USAR consorcio Sismo', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '55 days', LOCALTIMESTAMP - INTERVAL '60 days', LOCALTIMESTAMP - INTERVAL '55 days', 'Relevamiento estructural y rescate finalizados.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Rescatistas USAR consorcio Sismo');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 500, 'Medicamentos trauma consorcio Sismo', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '55 days', LOCALTIMESTAMP - INTERVAL '60 days', LOCALTIMESTAMP - INTERVAL '55 days', 'Hospital de campaña abastecido.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Medicamentos trauma consorcio Sismo');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 45, 'Paramédicos consorcio Sismo', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '55 days', LOCALTIMESTAMP - INTERVAL '60 days', LOCALTIMESTAMP - INTERVAL '55 days', 'Atención prehospitalaria cerrada.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Paramédicos consorcio Sismo');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 700, 'Alimentos Cáritas operativo Sismo', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '55 days', LOCALTIMESTAMP - INTERVAL '60 days', LOCALTIMESTAMP - INTERVAL '55 days', 'Distribución completa en albergues.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Alimentos Cáritas operativo Sismo');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 1100, 'Agua potable Cruz Roja operativo Sismo', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '55 days', LOCALTIMESTAMP - INTERVAL '60 days', LOCALTIMESTAMP - INTERVAL '55 days', 'Provisión de agua completada.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Movimiento sísmico moderado con daños estructurales y despliegue de albergues temporales.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua potable Cruz Roja operativo Sismo');

-- E10: Melchor Romero (FINALIZADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 380, 'Ropa de abrigo Melchor Romero', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '48 days', LOCALTIMESTAMP - INTERVAL '50 days', LOCALTIMESTAMP - INTERVAL '48 days', 'Entrega cerrada en parroquia local.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'ROPA_ABRIGO'
WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Ropa de abrigo Melchor Romero');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 320, 'Frazadas Melchor Romero', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '48 days', LOCALTIMESTAMP - INTERVAL '50 days', LOCALTIMESTAMP - INTERVAL '48 days', 'Frazadas entregadas.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS'
WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Frazadas Melchor Romero');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 10, 'Generadores Defensa Civil Melchor Romero', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '48 days', LOCALTIMESTAMP - INTERVAL '50 days', LOCALTIMESTAMP - INTERVAL '48 days', 'Servicio eléctrico normalizado.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES'
WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Generadores Defensa Civil Melchor Romero');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 180, 'Frazadas oferta alternativa Cruz Roja Romero', 'NO_SELECCIONADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 3, LOCALTIMESTAMP - INTERVAL '50 days', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS'
WHERE e.descripcion = 'Temporal de viento y granizo con asistencia de abrigo y energía de emergencia.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Frazadas oferta alternativa Cruz Roja Romero');

-- E11: Etcheverry (FINALIZADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 750, 'Agua potable escuelas rurales Etcheverry', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '40 days', LOCALTIMESTAMP - INTERVAL '42 days', LOCALTIMESTAMP - INTERVAL '40 days' - INTERVAL '8 hours', 'Entregas certificadas por directivos.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Abastecimiento preventivo de agua en escuelas rurales y puestos sanitarios.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua potable escuelas rurales Etcheverry');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 30, 'Voluntarios distribución Etcheverry', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '40 days', LOCALTIMESTAMP - INTERVAL '42 days', LOCALTIMESTAMP - INTERVAL '40 days' - INTERVAL '8 hours', 'Recorrido rural finalizado.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'VOLUNTARIOS'
WHERE e.descripcion = 'Abastecimiento preventivo de agua en escuelas rurales y puestos sanitarios.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Voluntarios distribución Etcheverry');

-- E12: Ringuelet y Gonnet (FINALIZADA - cobertura parcial aceptada + Consorcio Cáritas+Cruz Roja + 1 RETIRADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 520, 'Alimentos consorcio Ringuelet-Gonnet', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '27 days', LOCALTIMESTAMP - INTERVAL '30 days', LOCALTIMESTAMP - INTERVAL '27 days' - INTERVAL '8 hours', 'Raciones entregadas en clubes barriales.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Alimentos consorcio Ringuelet-Gonnet');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 900, 'Agua potable consorcio Ringuelet-Gonnet', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '27 days', LOCALTIMESTAMP - INTERVAL '30 days', LOCALTIMESTAMP - INTERVAL '27 days' - INTERVAL '8 hours', 'Entrega completa en zonas afectadas.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua potable consorcio Ringuelet-Gonnet');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 220, 'Medicamentos Médicos del Mundo Ringuelet', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '27 days', LOCALTIMESTAMP - INTERVAL '30 days', LOCALTIMESTAMP - INTERVAL '27 days' - INTERVAL '8 hours', 'Botiquines distribuidos en CAPS.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Medicamentos Médicos del Mundo Ringuelet');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 150, 'Lote retirado por reasignación de móvil', 'RETIRADA', c.fecha_creacion, NULL, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '31 days', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Anegamiento por tormenta extraordinaria en cuenca del arroyo Rodríguez.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Lote retirado por reasignación de móvil');

-- E13: Isla Santiago (FINALIZADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 40, 'Dotaciones bomberos Isla Santiago', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '19 days', LOCALTIMESTAMP - INTERVAL '22 days', LOCALTIMESTAMP - INTERVAL '19 days' - INTERVAL '20 hours', 'Humedal controlado sin reactivaciones.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Dotaciones bomberos Isla Santiago');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 20, 'Rescatistas ribereños Isla Santiago', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '19 days', LOCALTIMESTAMP - INTERVAL '22 days', LOCALTIMESTAMP - INTERVAL '19 days' - INTERVAL '20 hours', 'Operativo ribereño completado.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Rescatistas ribereños Isla Santiago');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 20, 'Paramédicos Isla Santiago', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '19 days', LOCALTIMESTAMP - INTERVAL '22 days', LOCALTIMESTAMP - INTERVAL '19 days' - INTERVAL '20 hours', 'Guardia sanitaria finalizada.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Paramédicos Isla Santiago');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 180, 'Medicamentos Isla Santiago', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '19 days', LOCALTIMESTAMP - INTERVAL '22 days', LOCALTIMESTAMP - INTERVAL '19 days' - INTERVAL '20 hours', 'Insumos consumidos y rendidos.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Incendio de humedales y juncales en Isla Santiago con asistencia sanitaria preventiva.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Medicamentos Isla Santiago');

-- E14: Los Hornos (EN_EJECUCION - proyecto EN_CURSO, mezcla FINALIZADA + ADJUDICADA, Consorcio Cáritas+Defensa Civil)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 850, 'Agua potable consorcio Los Hornos', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 4, LOCALTIMESTAMP - INTERVAL '12 days', LOCALTIMESTAMP - INTERVAL '14 days', LOCALTIMESTAMP - INTERVAL '12 days', 'Primer tramo de agua entregado.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua potable consorcio Los Hornos');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 70, 'Carpas consorcio Los Hornos', 'ADJUDICADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 3, LOCALTIMESTAMP - INTERVAL '14 days', LOCALTIMESTAMP - INTERVAL '14 days', NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Carpas consorcio Los Hornos');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 500, 'Alimentos Cruz Roja Los Hornos', 'ADJUDICADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 3, LOCALTIMESTAMP - INTERVAL '14 days', LOCALTIMESTAMP - INTERVAL '14 days', NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Alimentos Cruz Roja Los Hornos');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 25, 'Paramédicos Cruz Roja Los Hornos', 'FINALIZADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 4, LOCALTIMESTAMP - INTERVAL '11 days', LOCALTIMESTAMP - INTERVAL '14 days', LOCALTIMESTAMP - INTERVAL '11 days', 'Atención primaria concluida.'
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Desborde en cuenca Maldonado con operativo activo de entrega de suministros.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Paramédicos Cruz Roja Los Hornos');

-- E15: City Bell (ADJUDICADA - proyecto EN_CURSO, Consorcio Defensa Civil+Cáritas)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 18, 'Generadores consorcio City Bell', 'ADJUDICADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 3, LOCALTIMESTAMP - INTERVAL '8 days', LOCALTIMESTAMP - INTERVAL '8 days', NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'GENERADORES' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Generadores consorcio City Bell');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 400, 'Frazadas consorcio City Bell', 'ADJUDICADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, oc.id, 3, LOCALTIMESTAMP - INTERVAL '8 days', LOCALTIMESTAMP - INTERVAL '8 days', NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'FRAZADAS' JOIN dssd.oferta_conjunta oc ON oc.convocatoria_id = c.id
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Frazadas consorcio City Bell');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 150, 'Medicamentos Médicos del Mundo City Bell', 'ADJUDICADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 3, LOCALTIMESTAMP - INTERVAL '8 days', LOCALTIMESTAMP - INTERVAL '8 days', NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Medicamentos Médicos del Mundo City Bell');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 150, 'Medicamentos oferta alternativa Cruz Roja City Bell', 'NO_SELECCIONADA', c.fecha_creacion, c.fecha_creacion + 1, o.id, t.id, c.id, NULL, 3, LOCALTIMESTAMP - INTERVAL '8 days', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Cola de tornado en City Bell con daños en tendido eléctrico y viviendas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Medicamentos oferta alternativa Cruz Roja City Bell');

-- E16: Olmos Centro (EN_SELECCION - cobertura 100% VALIDADA lista para adjudicar por municipio)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 500, 'Bidones de agua Cáritas Olmos Centro', 'VALIDADA', c.fecha_creacion + 1, CURRENT_DATE - 1, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bidones de agua Cáritas Olmos Centro');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 400, 'Bidones de agua Cruz Roja Olmos Centro', 'VALIDADA', c.fecha_creacion + 1, CURRENT_DATE - 1, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bidones de agua Cruz Roja Olmos Centro');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 350, 'Alimentos Cruz Roja Olmos Centro', 'VALIDADA', c.fecha_creacion + 1, CURRENT_DATE - 1, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'ALIMENTOS'
WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Alimentos Cruz Roja Olmos Centro');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 200, 'Medicamentos Médicos del Mundo Olmos Centro', 'VALIDADA', c.fecha_creacion + 1, CURRENT_DATE - 1, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-MEDICOS-MUNDO' JOIN dssd.tipo_recurso t ON t.codigo = 'MEDICAMENTOS'
WHERE e.descripcion = 'Acumulación pluvial en casco urbano de Lisandro Olmos pendiente de selección de ofertas.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Medicamentos Médicos del Mundo Olmos Centro');

-- E17: Camino Rivadavia (REQUIERE_DECISION - cobertura insuficiente + 1 NO_HABILITADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 20, 'Rescatistas Bomberos Camino Rivadavia', 'VALIDADA', c.fecha_creacion + 1, CURRENT_DATE - 1, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'RESCATISTAS'
WHERE e.descripcion = 'Socavamiento de talud lindero al canal con cobertura de ofertas insuficiente.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Rescatistas Bomberos Camino Rivadavia');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 25, 'Carpas rechazadas por inspección técnica', 'NO_HABILITADA', c.fecha_creacion + 1, CURRENT_DATE - 1, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'CARPAS'
WHERE e.descripcion = 'Socavamiento de talud lindero al canal con cobertura de ofertas insuficiente.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Carpas rechazadas por inspección técnica');

-- E18: Pereyra Norte (EN_VALIDACION - ventana cerrada con ofertas PENDIENTE)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 25, 'Bomberos ataque inicial Pereyra Norte', 'PENDIENTE', c.fecha_creacion + 1, NULL, o.id, t.id, c.id, NULL, 1, LOCALTIMESTAMP - INTERVAL '2 days', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-BOMBEROS-LP' JOIN dssd.tipo_recurso t ON t.codigo = 'BOMBEROS'
WHERE e.descripcion = 'Incendio de malezas en borde norte del parque con ofertas en proceso de validación.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Bomberos ataque inicial Pereyra Norte');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 15, 'Paramédicos Cruz Roja Pereyra Norte', 'PENDIENTE', c.fecha_creacion + 1, NULL, o.id, t.id, c.id, NULL, 1, LOCALTIMESTAMP - INTERVAL '2 days', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'PARAMEDICOS'
WHERE e.descripcion = 'Incendio de malezas en borde norte del parque con ofertas en proceso de validación.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Paramédicos Cruz Roja Pereyra Norte');

-- E19: Abasto y Etcheverry (CONVOCATORIA_ABIERTA - ventana abierta + ofertas PENDIENTE + 1 RETIRADA)
INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 600, 'Agua potable Cáritas cinturón hortícola', 'PENDIENTE', CURRENT_DATE - 1, NULL, o.id, t.id, c.id, NULL, 1, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CARITAS' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua potable Cáritas cinturón hortícola');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 500, 'Agua potable Cruz Roja cinturón hortícola', 'PENDIENTE', CURRENT_DATE - 1, NULL, o.id, t.id, c.id, NULL, 1, LOCALTIMESTAMP - INTERVAL '1 day', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-CRUZ-ROJA' JOIN dssd.tipo_recurso t ON t.codigo = 'AGUA'
WHERE e.descripcion = 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Agua potable Cruz Roja cinturón hortícola');

INSERT INTO dssd.oferta (cantidad, descripcion, estado, fecha_creacion, fecha_validacion, organizacion_id, tipo_recurso_id, convocatoria_id, oferta_conjunta_id, numero_version, fecha_modificacion, fecha_adjudicacion, fecha_finalizacion, informe_finalizacion)
SELECT 30, 'Voluntarios Defensa Civil retirados temporalmente', 'RETIRADA', CURRENT_DATE - 1, NULL, o.id, t.id, c.id, NULL, 2, LOCALTIMESTAMP - INTERVAL '12 hours', NULL, NULL, NULL
FROM dssd.convocatoria c JOIN dssd.emergencia e ON e.id = c.emergencia_id JOIN dssd.organizacion o ON o.codigo_ong = 'ONG-DEFENSA-CIVIL' JOIN dssd.tipo_recurso t ON t.codigo = 'VOLUNTARIOS'
WHERE e.descripcion = 'Emergencia hídrica en cinturón hortícola con convocatoria abierta a ONGs.'
AND NOT EXISTS (SELECT 1 FROM dssd.oferta x WHERE x.convocatoria_id = c.id AND x.descripcion = 'Voluntarios Defensa Civil retirados temporalmente');
