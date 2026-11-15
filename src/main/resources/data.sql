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

-- Se cargan los usuarios de prueba, uno por perfil, vinculados a Bonita (CUIL ficticios)
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Olga', 'Olmos', '20000000011', 'OPERADOR_MUNICIPAL', 'olmos', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'olmos' OR cuil = '20000000011');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Carlos', 'Coordinador', '20000000029', 'COORDINADOR', 'agente_coordinador_1', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'agente_coordinador_1' OR cuil = '20000000029');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Rosa', 'Representante', '20000000037', 'ONG', 'caritas', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'caritas' OR cuil = '20000000037');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Raúl', 'Rescate', '20000000053', 'ONG', 'cruz_roja', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'cruz_roja' OR cuil = '20000000053');
INSERT INTO dssd.usuario (nombre, apellido, cuil, rol, usuario_bonita, fecha_creacion) SELECT 'Adrián', 'Auditor', '20000000045', 'AUDITOR', 'auditor', CURRENT_DATE WHERE NOT EXISTS (SELECT 1 FROM dssd.usuario WHERE usuario_bonita = 'auditor' OR cuil = '20000000045');

-- Se vinculan con Bonita los usuarios de prueba que ya existían sin usuario_bonita
UPDATE dssd.usuario SET usuario_bonita = 'olmos' WHERE cuil = '20000000011' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos');
UPDATE dssd.usuario SET usuario_bonita = 'agente_coordinador_1' WHERE cuil = '20000000029' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'agente_coordinador_1');
UPDATE dssd.usuario SET usuario_bonita = 'caritas' WHERE cuil = '20000000037' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'caritas');
UPDATE dssd.usuario SET usuario_bonita = 'cruz_roja' WHERE cuil = '20000000053' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'cruz_roja');
UPDATE dssd.usuario SET usuario_bonita = 'auditor' WHERE cuil = '20000000045' AND usuario_bonita IS NULL AND NOT EXISTS (SELECT 1 FROM dssd.usuario u WHERE u.usuario_bonita = 'auditor');

-- Se normalizan los roles locales de los usuarios de prueba (respaldo si Bonita no expone las membresías)
UPDATE dssd.usuario SET rol = 'OPERADOR_MUNICIPAL' WHERE usuario_bonita = 'olmos';
UPDATE dssd.usuario SET rol = 'COORDINADOR' WHERE usuario_bonita = 'agente_coordinador_1';
UPDATE dssd.usuario SET rol = 'ONG' WHERE usuario_bonita IN ('caritas', 'cruz_roja');
UPDATE dssd.usuario SET rol = 'AUDITOR' WHERE usuario_bonita = 'auditor';

-- Se vinculan los representantes con su organización
UPDATE dssd.usuario SET organizacion_id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-CARITAS') WHERE usuario_bonita = 'caritas' AND organizacion_id IS NULL;
UPDATE dssd.usuario SET organizacion_id = (SELECT MIN(o.id) FROM dssd.organizacion o WHERE o.codigo_ong = 'ONG-CRUZ-ROJA') WHERE usuario_bonita = 'cruz_roja' AND organizacion_id IS NULL;

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

-- Se carga un inventario inicial para las dos ONGs con usuario de prueba
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 500, 'Depósito central', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CARITAS' AND t.codigo = 'ALIMENTOS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 300, 'Donaciones de invierno', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CARITAS' AND t.codigo = 'FRAZADAS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 40, 'Equipos de guardia', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CRUZ-ROJA' AND t.codigo = 'PARAMEDICOS' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);
INSERT INTO dssd.inventario_recurso (organizacion_id, tipo_recurso_id, cantidad_disponible, descripcion, fecha_actualizacion) SELECT o.id, t.id, 2000, 'Bidones de 5 L', CURRENT_TIMESTAMP FROM dssd.organizacion o, dssd.tipo_recurso t WHERE o.codigo_ong = 'ONG-CRUZ-ROJA' AND t.codigo = 'AGUA' AND NOT EXISTS (SELECT 1 FROM dssd.inventario_recurso i WHERE i.organizacion_id = o.id AND i.tipo_recurso_id = t.id);

-- Se asignan al operador municipal las emergencias previas sin autor
UPDATE dssd.emergencia SET creado_por = (SELECT MIN(u.id) FROM dssd.usuario u WHERE u.usuario_bonita = 'olmos') WHERE creado_por IS NULL;
