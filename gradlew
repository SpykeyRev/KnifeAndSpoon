// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

part of dart.ui;

/// An opaque object representing a composited scene.
///
/// To create a Scene object, use a [SceneBuilder].
///
/// Scene objects can be displayed on the screen using the
/// [Window.render] method.
@pragma('vm:entry-point')
class Scene extends NativeFieldWrapperClass2 {
  /// This class is created by the engine, and should not be instantiated
  /// or extended directly.
  ///
  /// To create a Scene object, use a [SceneBuilder].
  @pragma('vm:entry-point')
  Scene._();


  /// Creates a raster image representation of the current state of the scene.
  /// This is a slow operation that is performed on a background thread.
  Future<Image> toImage(int width, int height) {
    if (width <= 0 || height <= 0)
      throw Exception('Invalid image dimensions.');
    return _futurize(
      (_Callback<Image> callback) => _toImage(width, height, callback)
    );
  }

  String _toImage(int width, int height, _Callback<Image> callback) native 'Scene_toImage';

  /// Releases the resources used by this scene.
  ///
  /// After calling this function, the scene is cannot be used further.
  void dispose() native 'Scene_dispose';
}

// Lightweight wrapper of a native layer object.
//
// This is used to provide a typed API for engine layers to prevent
// incompatible layers from being passed to [SceneBuilder]'s push methods.
// For example, this prevents a layer returned from `pushOpacity` from being
// passed as `oldLayer` to `pushTransform`. This is achieved by having one
// concrete subclass of this class per push method.
abstract class _EngineLayerWrapper implements EngineLayer {
  _EngineLayerWrapper._(this._nativeLayer);

  EngineLayer _nativeLayer;

  // Children of this layer.
  //
  // Null if this layer has no children. This field is populated only in debug
  // mode.
  List<_EngineLayerWrapper> _debugChildren;

  // Whether this layer was used as `oldLayer` in a past frame.
  //
  // It is illegal to use a layer object again after it is passed as an
  // `oldLayer` argument.
  bool _debugWasUsedAsOldLayer = false;

  bool _debugCheckNotUsedAsOldLayer() {
    assert(
      !_debugWasUsedAsOldLayer,
      'Layer $runtimeType was previously used as oldLayer.\n'
      'Once a layer is used as oldLayer, it may not be used again. Instead, '
      'after calling one of the SceneBuilder.push* methods and passing an oldLayer '
      'to it, use the layer returned by the method as oldLayer in subsequent '
      'frames.'
    );
    return true;
  }
}

/// An opaque handle to a transform engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushTransform].
///
/// {@template dart.ui.sceneBuilder.oldLayerCompatibility}
/// `oldLayer` parameter in [SceneBuilder] methods only accepts objects created
/// by the engine. [SceneBuilder] will throw an [AssertionError] if you pass it
/// a custom implementation of this class.
/// {@endtemplate}
class TransformEngineLayer extends _EngineLayerWrapper {
  TransformEngineLayer._(EngineLayer nativeLayer) : super._(nativeLayer);
}

/// An opaque handle to an offset engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushOffset].
///
/// {@macro dart.ui.sceneBuilder.oldLayerCompatibility}
class OffsetEngineLayer extends _EngineLayerWrapper {
  OffsetEngineLayer._(EngineLayer nativeLayer) : super._(nativeLayer);
}

/// An opaque handle to a clip rect engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushClipRect].
///
/// {@macro dart.ui.sceneBuilder.oldLayerCompatibility}
class ClipRectEngineLayer extends _EngineLayerWrapper {
  ClipRectEngineLayer._(EngineLayer nativeLayer) : super._(nativeLayer);
}

/// An opaque handle to a clip rounded rect engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushClipRRect].
///
/// {@macro dart.ui.sceneBuilder.oldLayerCompatibility}
class ClipRRectEngineLayer extends _EngineLayerWrapper {
  ClipRRectEngineLayer._(EngineLayer nativeLayer) : super._(nativeLayer);
}

/// An opaque handle to a clip path engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushClipPath].
///
/// {@macro dart.ui.sceneBuilder.oldLayerCompatibility}
class ClipPathEngineLayer extends _EngineLayerWrapper {
  ClipPathEngineLayer._(EngineLayer nativeLayer) : super._(nativeLayer);
}

/// An opaque handle to an opacity engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushOpacity].
///
/// {@macro dart.ui.sceneBuilder.oldLayerCompatibility}
class OpacityEngineLayer extends _EngineLayerWrapper {
  OpacityEngineLayer._(EngineLayer nativeLayer) : super._(nativeLayer);
}

/// An opaque handle to a color filter engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushColorFilter].
///
/// {@macro dart.ui.sceneBuilder.oldLayerCompatibility}
class ColorFilterEngineLayer extends _EngineLayerWrapper {
  ColorFilterEngineLayer._(EngineLayer nativeLayer) : super._(nativeLayer);
}

/// An opaque handle to a backdrop filter engine layer.
///
/// Instances of this class are created by [SceneBuilder.pushBackdropFilter].
///
/// {@macro dart.ui.scene